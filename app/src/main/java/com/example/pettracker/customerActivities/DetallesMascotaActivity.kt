package com.example.pettracker.customerActivities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.pettracker.R
import com.example.pettracker.domain.Datos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DetallesMascotaActivity : AppCompatActivity() {

    private lateinit var fotoMascota: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var petId: String? = null
    private var photoURI: Uri? = null
    private lateinit var tvName: TextView
    private lateinit var tvEdad: TextView
    private lateinit var tvEspecie: TextView
    private lateinit var tvRaza: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_mascota)

        petId = intent.getStringExtra("petId")

        auth = FirebaseAuth.getInstance()

        fotoMascota = findViewById(R.id.imageView)
        setupImagePickers()

        setUpViews()

        findViewById<ImageButton>(R.id.agregarFotoView).setOnClickListener {
            launchImagePicker()
        }

        findViewById<ImageButton>(R.id.tomarFotoView).setOnClickListener {
            handleCameraPermissions()
        }

        loadPetData()
    }

    private fun setUpViews() {
        tvName = findViewById(R.id.nombreMascotaView)
        tvEdad = findViewById(R.id.edadView)
        tvEspecie = findViewById(R.id.especieView)
        tvRaza = findViewById(R.id.razaView)
        tvDescripcion = findViewById(R.id.descrpcionView)
    }

    private fun setupImagePickers() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                fotoMascota.setImageURI(uri)
                uploadImageToFirebase(uri) // Subir la imagen a Firebase
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoURI?.let {
                    fotoMascota.setImageURI(it)
                    uploadImageToFirebase(it) // Subir la imagen a Firebase
                }
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val storageRef = Firebase.storage.reference.child("Mascotas/$userId/$petId")
            storageRef.putFile(imageUri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateProfileImageUriInDatabase(uri.toString(), userId)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Fallo al subir imagen", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfileImageUriInDatabase(imageUrl: String, userId: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Mascotas/$userId/$petId")
        val updates = hashMapOf<String, Any>("photoURI" to imageUrl)
        databaseRef.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al actualizar la imagen de perfil en la base de datos", Toast.LENGTH_SHORT).show()
        }
    }


    private fun launchImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun handleCameraPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.CAMERA
            ) -> {
                requestCameraPermission()
            }

            else -> {
                requestCameraPermission()
            }
        }
    }

    private fun requestCameraPermission() {
        requestPermissions(
            arrayOf(android.Manifest.permission.CAMERA),
            Datos.MY_PERMISSION_REQUEST_CAMERA
        )
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Datos.MY_PERMISSION_REQUEST_CAMERA -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Funcionalidades limitadas!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadPetData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val ref = petId?.let { database.getReference("Mascotas").child(userId).child(it) }

            if (ref != null) {
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val nombreMascota = dataSnapshot.child("nombre").getValue(String::class.java)
                        val edadMascota = dataSnapshot.child("edad").getValue(String::class.java)
                        val especieMascota = dataSnapshot.child("especie").getValue(String::class.java)
                        val razaMascota = dataSnapshot.child("raza").getValue(String::class.java)
                        val descripcionMascota = dataSnapshot.child("descripcion").getValue(String::class.java)
                        val photoURI = dataSnapshot.child("photoURI").getValue(String::class.java)

                        tvName.text = nombreMascota ?: "Nombre no disponible"
                        tvEdad.text = "$edadMascota meses"
                        tvEspecie.text = especieMascota
                        tvRaza.text = razaMascota
                        tvDescripcion.text = descripcionMascota

                        photoURI?.let {
                            Glide.with(this@DetallesMascotaActivity)
                                .load(it)
                                .into(fotoMascota)
                        } ?: run {
                            fotoMascota.setImageResource(R.drawable.icn_labrador)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@DetallesMascotaActivity, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}
