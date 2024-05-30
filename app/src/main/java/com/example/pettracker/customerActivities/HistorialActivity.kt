package com.example.pettracker.customerActivities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.pettracker.R
import com.example.pettracker.adapter.HistorialAdapter
import com.example.pettracker.domain.HistorialItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistorialActivity : AppCompatActivity() {

    private lateinit var mlista: ListView
    private lateinit var auth: FirebaseAuth
    private lateinit var mHistorialAdapter: HistorialAdapter

    private val solicitudesPaseo = mutableListOf<DataSnapshot>()
    private val usuarios = mutableMapOf<String, DataSnapshot>()
    private val ofertas = mutableMapOf<String, DataSnapshot>()
    private val mascotas = mutableMapOf<String, DataSnapshot>()
    private val database = FirebaseDatabase.getInstance()


    private val historialItems = mutableListOf<HistorialItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        auth = Firebase.auth

        setupListView()
        setupButtonPaseos()
        setupSettingsButton()

        cargarMascotas()
    }

    private fun cargarMascotas() {
        val mascotasRef = database.getReference("Mascotas")
        mascotasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    mascotas[snapshot.key!!] = snapshot
                    println("Mascotas ${snapshot.key}")
                }
                cargarOfertas()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun cargarOfertas() {
        val ofertasRef = database.getReference("Ofertas")
        ofertasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    ofertas[snapshot.key!!] = snapshot
                    println("Ofertas ${snapshot.key}")
                }
                cargarUsuarios()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun cargarUsuarios() {
        val usuariosRef = database.getReference("Usuarios")
        usuariosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    usuarios[snapshot.key!!] = snapshot
                    println("Usuarios ${snapshot.key}")
                }
                cargarDatos()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun cargarDatos() {
        val solicitudesRef = database.getReference("SolicitudesPaseo")
        solicitudesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    solicitudesPaseo.add(snapshot)
                }
                procesarSolicitudes()
                actualizarListView()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun procesarSolicitudes() {
        val currentUserUid = auth.currentUser?.uid ?: return

        for (solicitud in solicitudesPaseo) {

            val uidDueño = solicitud.child("uidDueño").getValue(String::class.java)
            val estado = solicitud.child("estado").getValue(String::class.java)

            if (uidDueño == currentUserUid && estado == "calificado") {

                val solicitudId = solicitud.key ?: ""
                val petIds = solicitud.child("petIds").children.map { it.getValue(String::class.java)!! }
                val uidPaseador = solicitud.child("uidPaseador").getValue(String::class.java) ?: ""
                val horaInicio = solicitud.child("horaInicio").getValue(String::class.java) ?: ""
                val horaFin = solicitud.child("horaFin").getValue(String::class.java) ?: ""
                val rating = solicitud.child("rating").getValue(Long::class.java) ?: ""
                val rate = rating.toString()
                val comentario = solicitud.child("comentario").getValue(String::class.java) ?: ""

                val nombrePaseador = obtenerNombrePaseador(uidPaseador)
                val precio = obtenerPrecioSolicitud(solicitudId, uidPaseador)
                val nombreMascotas = obtenerNombresMascotas(currentUserUid, petIds)
                val fecha = obtenerFechaActualEnFormatoString()


                historialItems.add(
                    HistorialItem(
                        nombreMascota = nombreMascotas,
                        fecha = fecha,
                        nombrePaseador = nombrePaseador,
                        hora_inicial = horaInicio,
                        hora_final = horaFin,
                        precio = "$$precio",
                        calificacion = rate.toInt(),
                        comentario = comentario
                    )
                )
            }
        }
    }

    private fun obtenerNombrePaseador(uidPaseador: String): String {
        val paseador = usuarios[uidPaseador]
        return paseador?.child("nombre")?.getValue(String::class.java) ?: ""

    }

    private fun obtenerPrecioSolicitud(solicitudId: String, uidPaseador: String): String {
        val oferta = ofertas[solicitudId]
        oferta?.children?.forEach { detalle ->
            if (detalle.child("userId").getValue(String::class.java) == uidPaseador) {
                return detalle.child("precio").getValue(String::class.java) ?: ""
            }
        }
        return ""
    }

    private fun obtenerFechaActualEnFormatoString(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaActual = Calendar.getInstance().time
        return dateFormat.format(fechaActual)
    }

    private fun obtenerNombresMascotas(uidDueño: String, petIds: List<String>): String {
        val mascota = mascotas[uidDueño]
        val nombresMascotas = petIds.mapNotNull { petId ->
            mascota?.child(petId)?.child("nombre")?.getValue(String::class.java)
        }
        return nombresMascotas.joinToString(", ")
    }

    private fun setupListView() {
        mlista = findViewById(R.id.historial)
        mHistorialAdapter = HistorialAdapter(this, historialItems)
        mlista.adapter = mHistorialAdapter

        mlista.setOnItemClickListener { _, _, position, _ ->
            val selectedPaseo = historialItems[position]
            val paseoBundle = createBundle(selectedPaseo)
            startDetailsActivity(paseoBundle)
        }
    }

    private fun setupButtonPaseos() {
        findViewById<Button>(R.id.buttonOption1).setOnClickListener {
            startNewActivity(HomeActivity::class.java)
        }
    }

    private fun setupSettingsButton() {
        findViewById<Button>(R.id.buttonOption3).setOnClickListener {
            startNewActivity(SettingsActivity::class.java)
        }
    }

    private fun createBundle(selectedPaseo: HistorialItem): Bundle {
        return Bundle().apply {
            putString("nombreMascota", selectedPaseo.nombreMascota)
            putString("fecha", selectedPaseo.fecha)
            putString("nombrePaseador", selectedPaseo.nombrePaseador)
            putString("hora_inicial", selectedPaseo.hora_inicial)
            putString("hora_final", selectedPaseo.hora_final)
            putString("precio", selectedPaseo.precio)
            putInt("calificacion", selectedPaseo.calificacion)
            putString("comentario", selectedPaseo.comentario)
        }
    }

    private fun startDetailsActivity(paseoBundle: Bundle) {
        val intent = Intent(this@HistorialActivity, DetallesHistorialActivity::class.java).apply {
            putExtras(paseoBundle)
        }
        startActivity(intent)
    }

    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(applicationContext, activityClass)
        startActivity(intent)
    }

    private fun actualizarListView() {
        mHistorialAdapter.notifyDataSetChanged()
    }
}
