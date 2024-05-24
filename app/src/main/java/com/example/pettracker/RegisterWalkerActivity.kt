package com.example.pettracker

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pettracker.domain.Datos
import com.example.pettracker.domain.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView

class RegisterWalkerActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etExperience: EditText
    private lateinit var imageView: CircleImageView
    private lateinit var buttonRegister: Button
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null
    private var auth: FirebaseAuth = Firebase.auth
    private var user: FirebaseUser? = null
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_walker)

        // Inicializa Firebase Auth y Database
        auth = Firebase.auth
        user = auth.currentUser

        initializeViews()
        setupButtons()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etExperience = findViewById(R.id.etExperience)
        buttonRegister = findViewById(R.id.button)
        imageView = findViewById(R.id.icn_paseador)
    }

    private fun setupButtons() {
        buttonRegister.setOnClickListener {
            handleRegistration()
        }

        setupImagePickers()
    }

    private fun validarCampos(): Boolean {
        var isValid = true

        // Validar nombre
        if (etName.text.toString().isEmpty()) {
            etName.error = "Falta ingresar nombre"
            isValid = false
        } else {
            etName.error = null
        }

        // Validar correo electrónico
        if (etEmail.text.toString().isEmpty() ||
            !Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches()
        ) {
            etEmail.error = "Correo electrónico inválido"
            isValid = false
        } else {
            etEmail.error = null
        }

        // Validar experiencia
        if (etExperience.text.toString().isEmpty()) {
            etExperience.error = "Falta ingresar apellido"
            isValid = false
        } else {
            etExperience.error = null
        }

        // Validar contraseña
        if (etPassword.text.toString().isEmpty()) {
            etPassword.error = "Falta ingresar contraseña"
            isValid = false
        } else {
            etPassword.error = null
        }

        return isValid
    }

    private fun handleRegistration() {
        if (validarCampos()) {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val experience = etExperience.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        user = auth.currentUser
                        val userId = user?.uid
                        // Guarda los datos del usuario en la base de datos
                        val database = Firebase.database
                        val ref = database.getReference("Paseadores").child(userId!!)
                        val userData = HashMap<String, Any>()
                        userData["nombre"] = name
                        userData["experiencia"] = experience

                        ref.setValue(userData)
                            .addOnSuccessListener {
                                cargarFotoPerfil(userId)

                                navigateToLogin()

                            }.addOnFailureListener { e ->
                                Snackbar.make(findViewById(android.R.id.content), "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
                            }
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Error: ${task.exception?.message}", Snackbar.LENGTH_LONG).show()
                    }
                }

        }
    }

    private fun cargarFotoPerfil(userId: String) {
        photoURI?.let { uri ->
            val storageRef = Firebase.storage.reference.child("Paseadores/$userId/profile")

            storageRef.putFile(uri)
                .addOnSuccessListener {
                    limpiarCampos()
                }
                .addOnFailureListener { e ->
                    println("No funciono la carga de la foto del usuario")
                }
        } ?: run {
            println("URI de foto nula")
        }
    }

    private fun limpiarCampos() {
        etName.setText("")
        etEmail.setText("")
        etPassword.setText("")
        etExperience.setText("")
        imageView.setImageResource(R.drawable.icn_foto_perfil)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupImagePickers() {

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                photoURI = uri
                imageView.setImageURI(uri)
                println("URI CORRECTO")
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoURI?.let {
                    imageView.setImageURI(it)
                }
            }
        }

        findViewById<ImageButton>(R.id.agregarFotoView).setOnClickListener {
            launchImagePicker()
        }

        findViewById<ImageButton>(R.id.tomarFotoView).setOnClickListener {
            handleCameraPermission()
        }
    }

    private fun launchImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun handleCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                requestCameraPermission()
            }
            else -> {
                requestCameraPermission()
            }
        }
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), Datos.MY_PERMISSION_REQUEST_CAMERA)
    }

    private fun openCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "Foto nueva")
            put(MediaStore.Images.Media.DESCRIPTION, "Tomada desde la aplicacion del Taller 2")
        }
        photoURI = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        photoURI?.let { uri ->
            takePictureLauncher.launch(uri)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Datos.MY_PERMISSION_REQUEST_CAMERA -> {
                handleCameraPermissionResult(grantResults)
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun handleCameraPermissionResult(grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Funcionalidades limitadas!", Toast.LENGTH_SHORT).show()
        }
    }
}
