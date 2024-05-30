package com.example.pettracker.walkerActivities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.pettracker.R
import com.example.pettracker.adapter.HistorialAdapter
import com.example.pettracker.adapter.HistorialWalkerAdapter
import com.example.pettracker.domain.HistorialItem
import com.example.pettracker.domain.HistorialWalkerItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistorialWalkerActivity : AppCompatActivity() {

    private lateinit var mlista: ListView
    private lateinit var auth: FirebaseAuth
    private lateinit var mHistorialAdapter: HistorialWalkerAdapter
    private val historialItems = mutableListOf<HistorialWalkerItem>()

    private val solicitudesPaseo = mutableListOf<DataSnapshot>()
    private val usuarios = mutableMapOf<String, DataSnapshot>()
    private val ofertas = mutableMapOf<String, DataSnapshot>()
    private val mascotas = mutableMapOf<String, DataSnapshot>()
    private val database = FirebaseDatabase.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_walker)

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

            val uidPaseador = solicitud.child("uidPaseador").getValue(String::class.java)
            println("UID Paseador $uidPaseador")
            println("Current User $currentUserUid")
            val estado = solicitud.child("estado").getValue(String::class.java)
            println("Estado $estado")



            if (uidPaseador == currentUserUid && (
                        estado == "calificado" ||
                        estado == "en curso" ||
                        estado == "finalizado")) {

                val uidDueño = solicitud.child("uidDueño").getValue(String::class.java)
                println("Uid dueño $uidDueño")

                val solicitudId = solicitud.key ?: ""
                println("solicitudid $uidPaseador")

                val horaInicio = solicitud.child("horaInicio").getValue(String::class.java) ?: ""
                val horaFin = solicitud.child("horaFin").getValue(String::class.java) ?: ""

                val nombreDuehno = obtenerNombreDuehno(uidDueño!!)
                val precio = obtenerPrecioSolicitud(solicitudId, uidPaseador)
                val fecha = obtenerFechaActualEnFormatoString()


                historialItems.add(
                    HistorialWalkerItem(
                        fecha = fecha,
                        nombreDuenho = nombreDuehno,
                        hora_inicial = horaInicio,
                        hora_final = horaFin,
                        precio = "$$precio",
                        estado = estado
                    )
                )
            }
        }
    }
    private fun obtenerNombreDuehno(uidDuenho: String): String {
        val dueno = usuarios[uidDuenho]
        return dueno?.child("nombre")?.getValue(String::class.java) ?: ""

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


    private fun setupListView() {
        mlista = findViewById(R.id.historial)
        mHistorialAdapter = HistorialWalkerAdapter(this, historialItems )
        mlista.adapter = mHistorialAdapter

        mlista.setOnItemClickListener { _, _, position, _ ->
            val selectedPaseo = historialItems[position]
            val paseoBundle = createBundle(selectedPaseo)
            startDetailsActivity(paseoBundle)
        }
    }

    private fun setupButtonPaseos() {
        findViewById<Button>(R.id.buttonOption1).setOnClickListener {
            startNewActivity(HomeWalkerActivity::class.java)
        }
    }

    private fun setupSettingsButton() {
        findViewById<Button>(R.id.buttonOption3).setOnClickListener {
            startNewActivity(SettingsWalkerActivity::class.java)
        }
    }

    private fun createBundle(selectedPaseo: HistorialWalkerItem): Bundle {
        return Bundle().apply {
            putString("fecha", selectedPaseo.fecha)
            putString("nombreDuenho", selectedPaseo.nombreDuenho)
            putString("hora_inicial", selectedPaseo.hora_inicial)
            putString("hora_final", selectedPaseo.hora_final)
            putString("precio", selectedPaseo.precio)
            putString("estado", selectedPaseo.estado)
        }
    }

    private fun startDetailsActivity(paseoBundle: Bundle) {
        val intent = Intent(this@HistorialWalkerActivity, DetallesHistorialWalkerActivity::class.java).apply {
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


