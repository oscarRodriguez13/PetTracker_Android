package com.example.pettracker.customerActivities

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitar_paseo)

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
        offersRef = database.getReference("Ofertas")

        // Cargar datos desde Firebase
        loadOffersFromFirebase()
    }

    private fun setupBarraHerramientas() {
        // Botón para ir al historial
        val historialButton = findViewById<Button>(R.id.buttonOption2)
        historialButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HistorialActivity::class.java
            )
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.buttonOption3)
        settingsButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                SettingsActivity::class.java
            )
            startActivity(intent)
        }

        val cancelButton = findViewById<Button>(R.id.btnCancelar)
        cancelButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HomeActivity::class.java
            )
            startActivity(intent)
        }
    }

    private fun abrirDetallePerfil(profile: Profile) {
        val intent = Intent(this, PerfilPaseadorActivity::class.java)
        intent.putExtra("profileName", profile.name)
        intent.putExtra("profilePrice", profile.price)
        startActivity(intent)
    }

    private fun loadOffersFromFirebase() {
        offersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                profiles.clear()
                for (solicitudSnapshot in snapshot.children) {
                    for (offerSnapshot in solicitudSnapshot.children) {
                        val precio = offerSnapshot.child("precio").getValue(String::class.java) ?: "No disponible"
                        val userId = offerSnapshot.child("userId").getValue(String::class.java) ?: "No disponible"
                        loadUserData(userId, precio)
                    }
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
