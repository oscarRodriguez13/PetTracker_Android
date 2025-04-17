package com.example.pettracker.customerActivities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pettracker.R
import com.example.pettracker.domain.SolicitudPaseo
import com.google.firebase.database.*

class FinalizarPaseoActivity : AppCompatActivity() {

    private var solicitudId: String? = null
    private var uidPaseador: String? = null
    private lateinit var database: DatabaseReference
    private lateinit var ratingBar: RatingBar
    private lateinit var comentarioEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finalizar_paseo)

        solicitudId = intent.getStringExtra("solicitudId")
        uidPaseador = intent.getStringExtra("uidPaseador")
        database = FirebaseDatabase.getInstance().reference

        ratingBar = findViewById(R.id.ratingBar)
        comentarioEditText = findViewById(R.id.et_comentario)

        setupPagarButton()
        setupRatingBar()
        setupHistorialButton()
        setupSettingsButton()
        fetchWalkerAndPetsData()
    }

    private fun setupPagarButton() {
        findViewById<Button>(R.id.button2).setOnClickListener {
            val rating = ratingBar.rating
            val comentario = comentarioEditText.text.toString()
            updateSolicitud(rating, comentario)
        }
    }

    private fun updateSolicitud(rating: Float, comentario: String) {
        val solicitudRef = database.child("SolicitudesPaseo").child(solicitudId!!)
        solicitudRef.child("estado").setValue("calificado")
        solicitudRef.child("rating").setValue(rating)
        solicitudRef.child("comentario").setValue(comentario).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Solicitud actualizada exitosamente", Toast.LENGTH_SHORT).show()
                startNewActivity(HomeActivity::class.java)
            } else {
                Toast.makeText(this, "Error actualizando la solicitud", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRatingBar() {
        ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                Toast.makeText(this, "Calificación seleccionada: $rating", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupHistorialButton() {
        findViewById<Button>(R.id.buttonOption2).setOnClickListener {
            startNewActivity(HistorialActivity::class.java)
        }
    }

    private fun setupSettingsButton() {
        findViewById<Button>(R.id.buttonOption3).setOnClickListener {
            startNewActivity(SettingsActivity::class.java)
        }
    }

    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(applicationContext, activityClass)
        startActivity(intent)
    }

    private fun fetchWalkerAndPetsData() {
        val solicitudRef = database.child("SolicitudesPaseo").child(solicitudId!!)
        solicitudRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val solicitud = dataSnapshot.getValue(SolicitudPaseo::class.java)
                solicitud?.let {
                    it.uidPaseador?.let { it1 -> fetchWalkerName(it1) }
                    fetchPetNames(it.petIds)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@FinalizarPaseoActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchWalkerName(uidPaseador: String) {
        val paseadorRef = database.child("Usuarios").child(uidPaseador)
        paseadorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val nombrePaseador = dataSnapshot.child("nombre").getValue(String::class.java)
                findViewById<TextView>(R.id.nombre_paseador).text = nombrePaseador
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@FinalizarPaseoActivity, "Error fetching walker name", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPetNames(petIds: List<String>) {
        val mascotasRef = database.child("Mascotas")
        val petNames = mutableListOf<String>()
        for (petId in petIds) {
            mascotasRef.child(petId).addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val nombreMascota = dataSnapshot.child("nombre").getValue(String::class.java)
                    nombreMascota?.let {
                        petNames.add(it)
                        findViewById<TextView>(R.id.mascotas_paseo).text = "Mascotas: ${petNames.joinToString(", ")}"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@FinalizarPaseoActivity, "Error fetching pet names", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
