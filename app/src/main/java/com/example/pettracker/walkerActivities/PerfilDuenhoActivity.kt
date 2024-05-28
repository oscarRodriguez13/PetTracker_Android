package com.example.pettracker.walkerActivities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pettracker.R
import com.example.pettracker.customerActivities.SettingsActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PerfilDuenhoActivity : AppCompatActivity() {

    private lateinit var nombreDuenoText: TextView
    private lateinit var direccionText: TextView
    private lateinit var mascotasList: ListView
    private lateinit var fotoPerfilImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_duenho)

        nombreDuenoText = findViewById(R.id.nombre_paseador)
        direccionText = findViewById(R.id.direccion)
        mascotasList = findViewById(R.id.listaMascotas)
        fotoPerfilImageView = findViewById(R.id.foto_perfil_paseador)

        val intent = intent
        val uid = intent.getStringExtra("uid")

        if (uid != null) {
            loadUserData(uid)
            loadProfileImage(uid)
            loadUserPets(uid)
        }

        setupButtons()
    }

    private fun setupButtons() {
        val historialButton = findViewById<Button>(R.id.buttonOption2)
        historialButton.setOnClickListener {
            navigateToHistorial()
        }

        val settingsButton = findViewById<Button>(R.id.buttonOption3)
        settingsButton.setOnClickListener {
            navigateToSettings()
        }

        val aceptarButton = findViewById<Button>(R.id.btnAceptar)
        aceptarButton.setOnClickListener {
            navigateToHomeWalker()
        }
    }

    private fun navigateToHistorial() {
        val intent = Intent(this, HistorialWalkerActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToHomeWalker() {
        val intent = Intent(this, HomeWalkerActivity::class.java)
        startActivity(intent)
    }

    private fun loadUserData(uid: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("Usuarios").child(uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nombre = snapshot.child("nombre").getValue(String::class.java)
                    val direccion = snapshot.child("direccion").getValue(String::class.java)

                    nombreDuenoText.text = nombre ?: "Nombre no disponible"
                    direccionText.text = direccion ?: "Dirección no disponible"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun loadProfileImage(uid: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val profileImageRef = storageRef.child("Usuarios/$uid/profile")

        profileImageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(fotoPerfilImageView)
        }.addOnFailureListener {
            // Handle error
        }
    }

    private fun loadUserPets(uid: String) {
        val database = FirebaseDatabase.getInstance()
        val petsRef = database.getReference("Mascotas").child(uid)

        petsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val petNames = ArrayList<String>()
                    for (petSnapshot in snapshot.children) {
                        val petName = petSnapshot.child("nombre").getValue(String::class.java)
                        petName?.let {
                            petNames.add(it)
                        }
                    }
                    val adapter = ArrayAdapter(this@PerfilDuenhoActivity, android.R.layout.simple_list_item_1, petNames)
                    mascotasList.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
