package com.example.pettracker.walkerActivities

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
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
import com.example.pettracker.R
import com.example.pettracker.adapter.SolicitudPaseoAdapter
import com.example.pettracker.customerActivities.DetallesMascotaActivity
import com.example.pettracker.domain.Datos
import com.example.pettracker.domain.Profile
import com.example.pettracker.generalActivities.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Arrays

class SettingsWalkerActivity : AppCompatActivity() {

    private var isNotified = false
    private lateinit var fotoPaseador: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_walker)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        fotoPaseador = findViewById(R.id.icn_perfil)
        setupImagePickers()

        findViewById<ImageButton>(R.id.agregarFotoView).setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        findViewById<ImageButton>(R.id.tomarFotoView).setOnClickListener {
            handleCameraPermissions()
        }

        val userEmail = intent.getStringExtra("EMAIL")

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val profiles = Arrays.asList(
            Profile("a", null, "Anónimo", "Muy buen paseador, perfecto como siempre"),
            Profile("b", null, "Anónimo", "Paseo regular, Rocky volvió inquieto"),
            Profile("a", null, "Anónimo", "Buen paseo, pero Toby necesitaba más agua")
        )

        val adapter = SolicitudPaseoAdapter(profiles) { profile ->
            abrirDetallePerfil(profile)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.buttonOption2).setOnClickListener {
            startActivity(Intent(applicationContext, HistorialWalkerActivity::class.java))
        }

        findViewById<Button>(R.id.buttonOption1).setOnClickListener {
            startActivity(Intent(applicationContext, HomeWalkerActivity::class.java))
        }

        findViewById<CircleImageView>(R.id.icn_logout).setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
        }

        findViewById<CircleImageView>(R.id.icn_notificacion).setOnClickListener {
            toggleNotificationIcon()
        }

        val ratingBar = findViewById<RatingBar>(R.id.calificacionView)
        ratingBar.isClickable = false
        ratingBar.isFocusable = false
        ratingBar.stepSize = 1f
        ratingBar.numStars = 5
        ratingBar.rating = 4f
        ratingBar.isEnabled = false

        userId?.let { loadUserData(it) }
    }

    private fun setupImagePickers() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                fotoPaseador.setImageURI(uri)
                uploadImageToFirebase(uri)
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoURI?.let {
                    fotoPaseador.setImageURI(it)
                    uploadImageToFirebase(it)
                }
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val storageRef = Firebase.storage.reference.child("Usuarios/$userId/profile")
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
        val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios/$userId")
        val updates = hashMapOf<String, Any>("photoURI" to imageUrl)
        databaseRef.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al actualizar la imagen de perfil en la base de datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCameraPermissions() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA) -> {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), Datos.MY_PERMISSION_REQUEST_CAMERA)
            }
            else -> {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), Datos.MY_PERMISSION_REQUEST_CAMERA)
            }
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
            else -> {}
        }
    }

    private fun toggleNotificationIcon() {
        val notifyButton = findViewById<CircleImageView>(R.id.icn_notificacion)
        if (isNotified) {
            notifyButton.setImageResource(R.drawable.icn_notificacion_inactiva)
        } else {
            notifyButton.setImageResource(R.drawable.icn_notificacion)
        }
        isNotified = !isNotified
    }

    private fun loadUserData(userId: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios/$userId")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userName = dataSnapshot.child("nombre").getValue(String::class.java)
                    val userExp = dataSnapshot.child("experiencia").getValue(String::class.java)

                    findViewById<TextView>(R.id.txt_nombre).text = userName
                    findViewById<TextView>(R.id.txt_email).text = auth.currentUser?.email.toString()
                    findViewById<TextView>(R.id.txt_descripcion).text = "Experiencia: $userExp"

                    // Cargar la imagen de perfil desde Firebase Storage
                    val storageRef = Firebase.storage.reference.child("Usuarios/$userId/profile")
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(this@SettingsWalkerActivity)
                            .load(uri)
                            .into(fotoPaseador)
                    }.addOnFailureListener {
                        fotoPaseador.setImageResource(R.drawable.icn_labrador)
                    }

                } else {
                    Toast.makeText(this@SettingsWalkerActivity, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@SettingsWalkerActivity, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun abrirDetallePerfil(profile: Profile) {
        val intent = Intent(this, DetallesMascotaActivity::class.java)
        intent.putExtra("profileName", profile.name)
        intent.putExtra("profilePrice", profile.price)
        startActivity(intent)
    }
}
