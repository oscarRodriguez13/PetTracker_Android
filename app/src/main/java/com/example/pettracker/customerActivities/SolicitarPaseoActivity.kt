package com.example.pettracker.customerActivities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pettracker.R
import com.example.pettracker.adapter.SolicitudPaseoAdapter
import com.example.pettracker.domain.Profile
import com.google.firebase.database.*

class SolicitarPaseoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SolicitudPaseoAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var offersRef: DatabaseReference
    private val profiles = ArrayList<Profile>()
    private var solicitudId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitar_paseo)

        solicitudId = intent.getStringExtra("solicitudId")

        setupBarraHerramientas()

        // Configuración del RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        adapter = SolicitudPaseoAdapter(profiles) { profile ->
            abrirDetallePerfil(profile)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar Firebase
        database = FirebaseDatabase.getInstance()

        println("Solicitud id $solicitudId")

        // Cargar datos desde Firebase si solicitudId no es nulo
        if (solicitudId != null) {
            offersRef = database.getReference("Ofertas").child(solicitudId!!)
            loadOffersFromFirebase()
        } else {
            Toast.makeText(this, "Solicitud ID no proporcionada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBarraHerramientas() {
        // Botón para ir al historial
        val historialButton = findViewById<Button>(R.id.buttonOption2)
        historialButton.setOnClickListener {
            val intent = Intent(applicationContext, HistorialActivity::class.java)
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.buttonOption3)
        settingsButton.setOnClickListener {
            val intent = Intent(applicationContext, SettingsActivity::class.java)
            startActivity(intent)
        }

        val cancelButton = findViewById<Button>(R.id.btnCancelar)
        cancelButton.setOnClickListener {
            if (solicitudId != null) {
                eliminarSolicitudYOfertas(solicitudId!!)
            } else {
                Toast.makeText(this, "Solicitud ID no proporcionada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarSolicitudYOfertas(solicitudId: String) {
        val solicitudesRef = database.getReference("SolicitudesPaseo").child(solicitudId)
        val ofertasRef = database.getReference("Ofertas").child(solicitudId)

        solicitudesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val petIds = snapshot.child("petIds").children.map { it.value.toString() }
                val uidDueño = snapshot.child("uidDueño").getValue(String::class.java) ?: ""

                solicitudesRef.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("Solicitud eliminada en SolicitudesPaseos")
                        ofertasRef.removeValue().addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                actualizarEstadoMascotas(uidDueño, petIds)
                                Toast.makeText(this@SolicitarPaseoActivity, "Solicitud cancelada con éxito", Toast.LENGTH_SHORT).show()
                                val intent = Intent(applicationContext, HomeActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@SolicitarPaseoActivity, "Error al cancelar la solicitud en Ofertas", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@SolicitarPaseoActivity, "Error al cancelar la solicitud en SolicitudesPaseos", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SolicitarPaseoActivity, "Error al obtener los datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun actualizarEstadoMascotas(uidDueño: String, petIds: List<String>) {
        println("Entre a actualizar estados")

        println("UID $uidDueño")
        println("Numero de mascotas ${petIds.size}")

        val mascotasRef = database.getReference("Mascotas").child(uidDueño)
        mascotasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (petId in petIds) {
                    println("Buscamos el pet id $petId")
                    if (snapshot.hasChild(petId)) {
                        println("Encontramos a la mascota con id $petId")
                        mascotasRef.child(petId).child("estado").setValue("disponible")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SolicitarPaseoActivity, "Error al actualizar el estado de las mascotas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun abrirDetallePerfil(profile: Profile) {
        val intent = Intent(this, PerfilPaseadorActivity::class.java)
        intent.putExtra("profileUid", profile.uid)
        intent.putExtra("solicitudId", solicitudId)
        startActivity(intent)
    }

    private fun loadOffersFromFirebase() {
        offersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                profiles.clear()
                for (offerSnapshot in snapshot.children) {
                    val precio = offerSnapshot.child("precio").getValue(String::class.java) ?: "No disponible"
                    val userId = offerSnapshot.child("userId").getValue(String::class.java) ?: "No disponible"
                    loadUserData(userId, precio)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SolicitarPaseoActivity, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadUserData(userId: String, precio: String) {
        val userRef = database.getReference("Usuarios").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("nombre").getValue(String::class.java) ?: "Nombre no disponible"
                val profile = Profile(userId, null, nombre, precio)
                profiles.add(profile)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SolicitarPaseoActivity, "Error al cargar datos del usuario: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
