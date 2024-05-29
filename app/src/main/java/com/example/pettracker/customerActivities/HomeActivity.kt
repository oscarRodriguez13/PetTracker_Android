package com.example.pettracker.customerActivities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pettracker.R
import com.example.pettracker.adapter.HomeAdapter
import com.example.pettracker.adapter.ProgramarPaseoAdapter
import com.example.pettracker.domain.ProgramarPaseoItem
import com.example.pettracker.domain.SolicitudItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var selectedPetIds: List<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var solicitudId: String? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HomeAdapter
    private var paseoItems = mutableListOf<SolicitudItem>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = Firebase.auth

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HomeAdapter(paseoItems) { paseoItem ->
            val intent = Intent(this, SolicitarPaseoActivity::class.java)
            intent.putExtra("solicitudId", paseoItem.solicitudId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadSolicitudesPaseo()

        setupTimePickers()
        setupSolicitudPaseo()
        setupHistorialButton()
        setupSettingsButton()
        setupSelectOptionsButton()
    }

    private fun loadSolicitudesPaseo() {
        val database = FirebaseDatabase.getInstance().getReference("SolicitudesPaseo")
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevaLista = mutableListOf<SolicitudItem>()

                for (data in snapshot.children) {
                    val solicitudId = data.key ?: continue
                    val horaInicio = data.child("horaInicio").getValue(String::class.java)
                    val horaFin = data.child("horaFin").getValue(String::class.java)
                    val uidDueño = data.child("uidDueño").getValue(String::class.java)
                    val petIds = data.child("petIds").children.map { it.getValue(String::class.java) ?: "" }
                    val cantidad = petIds.size.toString()

                    if (uidDueño == userId && horaInicio != null && horaFin != null) {
                        val paseoItem = SolicitudItem(
                            solicitudId,
                            horaInicio,
                            horaFin,
                            cantidad
                        )
                        nuevaLista.add(paseoItem)
                    }
                }

                adapter.actualizarLista(nuevaLista)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    @SuppressLint("SetTextI18n")
    private fun setupTimePickers() {
        val etHoraInicial = findViewById<EditText>(R.id.etHoraInicial)
        val etHoraFinal = findViewById<EditText>(R.id.etHoraFinal)

        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)

        setupTimePicker(etHoraInicial, hour, minute) { selectedTime ->
            etHoraInicial.setText("Hora inicial: $selectedTime")
        }

        setupTimePicker(etHoraFinal, hour, minute) { selectedTime ->
            etHoraFinal.setText("Hora final: $selectedTime")
        }
    }

    private fun setupTimePicker(editText: EditText, hour: Int, minute: Int, onTimeSet: (String) -> Unit) {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                onTimeSet(selectedTime)
            },
            hour,
            minute,
            true
        )

        editText.setOnClickListener {
            timePickerDialog.show()
        }
    }

    private fun setupSolicitudPaseo() {
        val btnSolicitudPaseo = findViewById<Button>(R.id.btn_solicitud_paseo)

        btnSolicitudPaseo.setOnClickListener {
            val etHoraInicial = findViewById<EditText>(R.id.etHoraInicial)
            val etHoraFinal = findViewById<EditText>(R.id.etHoraFinal)

            if (verificarCamposLlenos(etHoraInicial, etHoraFinal)) {
                // Verificar si hay mascotas disponibles
                if (selectedPetIds.isEmpty()) {
                    Toast.makeText(this, "No tienes mascotas disponibles en este momento", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val horaInicial = etHoraInicial.text.toString().substringAfter(": ").trim()
                val horaFinal = etHoraFinal.text.toString().substringAfter(": ").trim()

                // Verificar si la hora inicial no es menor que la hora actual
                if (isHoraInicialValida(horaInicial)) {
                    // Verificar si la hora final es mayor que la hora inicial
                    if (isHoraFinalMayorQueInicial(horaInicial, horaFinal)) {
                        // Verificar permisos y obtener ubicación
                        if (checkLocationPermission()) {
                            getLocationAndCreateSolicitud(
                                horaInicial,
                                horaFinal
                            )
                        } else {
                            requestLocationPermission()
                        }
                    } else {
                        Toast.makeText(this, "La hora final debe ser mayor que la hora inicial", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "La hora inicial no puede ser menor que la hora actual", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isHoraInicialValida(horaInicial: String): Boolean {
        val horaActual = Calendar.getInstance()
        val horaActualInt = horaActual.get(Calendar.HOUR_OF_DAY)
        val minutoActualInt = horaActual.get(Calendar.MINUTE)

        val horaInicialSplit = horaInicial.split(":")
        val horaInicialInt = horaInicialSplit[0].toInt()
        val minutoInicialInt = horaInicialSplit[1].toInt()

        if (horaInicialInt > horaActualInt) {
            return true
        } else if (horaInicialInt == horaActualInt && minutoInicialInt >= minutoActualInt) {
            return true
        }
        return false
    }

    private fun isHoraFinalMayorQueInicial(horaInicial: String, horaFinal: String): Boolean {
        val horaInicialSplit = horaInicial.split(":")
        val horaFinalSplit = horaFinal.split(":")
        val horaInicialInt = horaInicialSplit[0].toInt()
        val minutoInicialInt = horaInicialSplit[1].toInt()
        val horaFinalInt = horaFinalSplit[0].toInt()
        val minutoFinalInt = horaFinalSplit[1].toInt()

        if (horaFinalInt > horaInicialInt) {
            return true
        } else if (horaFinalInt == horaInicialInt && minutoFinalInt > minutoInicialInt) {
            return true
        }
        return false
    }

    private fun setupHistorialButton() {
        findViewById<Button>(R.id.buttonOption2).setOnClickListener {
            startActivity(Intent(applicationContext, HistorialActivity::class.java))
        }
    }

    private fun setupSettingsButton() {
        findViewById<Button>(R.id.buttonOption3).setOnClickListener {
            startActivity(Intent(applicationContext, SettingsActivity::class.java))
        }
    }

    private fun setupSelectOptionsButton() {
        val tvOption = findViewById<TextView>(R.id.tv_option)
        findViewById<Button>(R.id.button_options).setOnClickListener {
            loadPetOptions(tvOption)
        }
    }

    private fun loadPetOptions(tvOption: TextView) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("Mascotas/$userId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pets = ArrayList<String>()
                val petIds = ArrayList<String>()
                snapshot.children.forEach {
                    val nombre = it.child("nombre").getValue(String::class.java)
                    val estado = it.child("estado").getValue(String::class.java)
                    val petId = it.key
                    if (nombre != null && petId != null && estado == "disponible") {
                        pets.add(nombre)
                        petIds.add(petId)
                    }
                }
                showOptionsDialog(tvOption, pets.toTypedArray(), petIds.toTypedArray())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showOptionsDialog(tvOption: TextView, options: Array<String>, petIds: Array<String>) {
        val selectedOptions = BooleanArray(options.size)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccione las opciones")

        builder.setMultiChoiceItems(options, selectedOptions) { _, which, isChecked ->
            selectedOptions[which] = isChecked
        }

        builder.setPositiveButton("Aceptar") { _, _ ->
            val selectedItemsText = options.indices
                .filter { selectedOptions[it] }
                .joinToString { options[it] }

            selectedPetIds = petIds.indices
                .filter { selectedOptions[it] }
                .map { petIds[it] }

            tvOption.text = "Mascotas seleccionadas: $selectedItemsText"
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun verificarCamposLlenos(vararg campos: EditText) = campos.all { it.text.toString().trim().isNotEmpty() }

    private fun checkLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocation == PackageManager.PERMISSION_GRANTED && coarseLocation == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLocationAndCreateSolicitud(horaInicio: String, horaFin: String) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    uploadLocationAndCreateSolicitud(location, horaInicio, horaFin)
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener la ubicación: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadLocationAndCreateSolicitud(location: Location, horaInicio: String, horaFin: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val userLocationData = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude
        )

        val database = FirebaseDatabase.getInstance().getReference("Usuarios/$userId")
        database.child("ubicacion").setValue(userLocationData)
            .addOnSuccessListener {
                crearSolicitudPaseo(userId, horaInicio, horaFin)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir la ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun crearSolicitudPaseo(userId: String, horaInicio: String, horaFin: String) {
        val database = FirebaseDatabase.getInstance().getReference("SolicitudesPaseo")
        solicitudId = database.push().key

        if (solicitudId == null) {
            Toast.makeText(this, "Error al generar ID de solicitud", Toast.LENGTH_SHORT).show()
            return
        }

        val solicitudData = hashMapOf(
            "uidDueño" to userId,
            "petIds" to selectedPetIds,
            "horaInicio" to horaInicio,
            "horaFin" to horaFin,
            "estado" to "no iniciado"
        )

        database.child(solicitudId!!).setValue(solicitudData)
            .addOnSuccessListener {
                actualizarEstadoMascotas(userId)
                Toast.makeText(this, "Solicitud creada exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear solicitud: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarEstadoMascotas(userId: String) {
        val mascotasRef = FirebaseDatabase.getInstance().getReference("Mascotas/$userId")

        for (petId in selectedPetIds) {
            mascotasRef.child(petId).child("estado").setValue("en paseo")
                .addOnSuccessListener {
                    Toast.makeText(this, "Estado de la mascota $petId actualizado a 'en paseo'", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar estado de la mascota $petId: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, "Permisos de ubicación concedidos", Toast.LENGTH_SHORT).show()
                    val etHoraInicial = findViewById<EditText>(R.id.etHoraInicial)
                    val etHoraFinal = findViewById<EditText>(R.id.etHoraFinal)
                    getLocationAndCreateSolicitud(etHoraInicial.text.toString().substringAfter(": ").trim(), etHoraFinal.text.toString().substringAfter(": ").trim())
                } else {
                    Toast.makeText(this, "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
