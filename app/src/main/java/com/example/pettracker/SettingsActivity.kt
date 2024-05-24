package com.example.pettracker

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pettracker.domain.Datos
import com.example.pettracker.domain.MascotasAdapter
import com.example.pettracker.domain.Pet
import com.example.pettracker.domain.Profile
import com.example.pettracker.domain.SolicitudPaseoAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView

class SettingsActivity : AppCompatActivity() {

    private var isNotified = false
    private lateinit var fotoPaseador: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPets: TextView
    private var photoURI: Uri? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        fotoPaseador = findViewById(R.id.icn_perfil)
        auth = FirebaseAuth.getInstance()

        setupImagePickers()
        setupButtons()
        setupRecyclerView()

        // Cargar datos del usuario desde Firebase
        loadUserData()
    }

    private fun setupImagePickers() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                fotoPaseador.setImageURI(uri)
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoURI?.let {
                    fotoPaseador.setImageURI(it)
                }
            }
        }
    }

    private fun setupButtons() {

        tvName = findViewById(R.id.txt_nombre)
        tvEmail = findViewById(R.id.txt_email)
        tvPets = findViewById(R.id.txt_Mascotas)

        findViewById<ImageButton>(R.id.agregarFotoView).setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        findViewById<ImageButton>(R.id.tomarFotoView).setOnClickListener {
            handleCameraPermission()
        }

        findViewById<Button>(R.id.buttonOption2).setOnClickListener {
            navigateToHistorial()
        }

        findViewById<Button>(R.id.buttonOption1).setOnClickListener {
            navigateToHome()
        }

        findViewById<CircleImageView>(R.id.icn_logout).setOnClickListener {
            navigateToLogin()
        }

        findViewById<CircleImageView>(R.id.icn_notificacion).setOnClickListener {
            toggleNotification()
        }
    }

    private fun handleCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.CAMERA
            ) -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    Datos.MY_PERMISSION_REQUEST_CAMERA
                )
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    Datos.MY_PERMISSION_REQUEST_CAMERA
                )
            }
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val pets = mutableListOf<Pet>()
        val adapter = MascotasAdapter(pets) { pet ->
            abrirDetallePerfil(pet)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Firebase Database reference
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val ref = database.child("Usuarios").child(userId).child("mascotas")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    pets.clear()
                    for (petSnapshot in dataSnapshot.children) {
                        val name = petSnapshot.child("nombre").getValue(String::class.java) ?: "Sin nombre"
                        val breed = petSnapshot.child("raza").getValue(String::class.java) ?: "Sin raza"
                        val pet = Pet(userId, petSnapshot.key.toString(), null, name, breed) // Usa una imagen por defecto
                        pets.add(pet)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@SettingsActivity, "Error al cargar perfiles de mascotas", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Foto nueva")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Tomada desde la aplicacion del Taller 2")
        photoURI = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        photoURI?.let { uri ->
            takePictureLauncher.launch(uri)
        }
    }

    private fun navigateToHistorial() {
        val intent = Intent(applicationContext, HistorialActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToHome() {
        val intent = Intent(applicationContext, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun toggleNotification() {
        val notifyButton = findViewById<CircleImageView>(R.id.icn_notificacion)
        if (isNotified) {
            notifyButton.setImageResource(R.drawable.icn_notificacion_inactiva)
        } else {
            notifyButton.setImageResource(R.drawable.icn_notificacion)
        }
        isNotified = !isNotified
    }

    private fun abrirDetallePerfil(pet: Pet) {
        val intent = Intent(this, DetallesMascotaActivity::class.java)
        intent.putExtra("profileName", pet.name)
        intent.putExtra("profilePrice", pet.price)
        startActivity(intent)
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("Usuarios").child(userId)

            val profileRef = Firebase.storage.reference.child("Usuarios").child(userId).child("profile")

            profileRef.downloadUrl.addOnSuccessListener { uri ->
                val profileImageUrl = uri.toString()

                // Cargar la imagen usando Glide
                Glide.with(this@SettingsActivity) // Utiliza el contexto del itemView
                    .load(profileImageUrl) // Utiliza la URL de la imagen
                    .into(fotoPaseador)

            }.addOnFailureListener {
                fotoPaseador.setImageResource(R.drawable.icn_labrador)
            }

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userName = dataSnapshot.child("nombre").getValue(String::class.java)
                    val userEmail = auth.currentUser?.email
                    val petsCount = dataSnapshot.child("mascotas").childrenCount.toInt()

                    tvName.text = userName ?: "Nombre no disponible"
                    tvEmail.text = userEmail ?: "Correo no disponible"
                    tvPets.text = "Mascotas: $petsCount"
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@SettingsActivity, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}
