package com.example.pettracker.walkerActivities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pettracker.R
import com.example.pettracker.customerActivities.SettingsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class PerfilDuenhoActivity : AppCompatActivity() {

    private lateinit var nombreDuenoText: TextView
    private lateinit var direccionText: TextView
    private lateinit var mascotasList: ListView
    private lateinit var fotoPerfilImageView: ImageView
    private lateinit var etPrecio: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_duenho)

        nombreDuenoText = findViewById(R.id.nombre_paseador)
        direccionText = findViewById(R.id.direccion)
        mascotasList = findViewById(R.id.listaMascotas)
        fotoPerfilImageView = findViewById(R.id.foto_perfil_paseador)
        etPrecio = findViewById(R.id.etPrecio)

        auth = FirebaseAuth.getInstance()

        val intent = intent
        val uid = intent.getStringExtra("usuarioUid")
        val solicitudId = intent.getStringExtra("solicitudId")

        if (uid != null) {
            loadUserData(uid)
            loadProfileImage(uid)
            loadUserPets(uid)
        }

        setupButtons(solicitudId)
    }

    private fun setupButtons(solicitudId: String?) {
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
            if (etPrecio.text.isNotEmpty()) {
                solicitudId?.let { id ->
                    createOffer(id, etPrecio.text.toString())
                }
            } else {
                Toast.makeText(this, "Por favor ingresa un precio", Toast.LENGTH_SHORT).show()
            }
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

    private fun createOffer(solicitudId: String, precio: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val database = FirebaseDatabase.getInstance()
            val offersRef = database.getReference("Ofertas").child(solicitudId)
            val newOfferRef = offersRef.push()

            val offerData = mapOf(
                "userId" to user.uid,
                "precio" to precio
            )

            newOfferRef.setValue(offerData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navigateToHomeWalker()
                } else {
                    Toast.makeText(this, "Error al enviar la oferta", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}
