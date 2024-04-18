package com.example.pettracker

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pettracker.domain.Datos

class RegisterUserActivity : AppCompatActivity() {

    private lateinit var fotoPaseador: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()
        setupButtons()
    }

    private fun initializeViews() {
        fotoPaseador = findViewById(R.id.icn_paseador)
    }

    private fun setupButtons() {
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etNumberPets)
        val button = findViewById<Button>(R.id.button)

        button.setOnClickListener {
            handleRegister(etName, etEmail, etPassword, etConfirmPassword)
        }

        setupImagePickers()
    }

    private fun handleRegister(etName: EditText, etEmail: EditText, etPassword: EditText, etConfirmPassword: EditText) {
        if (verificarCamposLlenos(etName, etEmail, etPassword, etConfirmPassword)) {
            navigateToRegisterPetData(etName.text.toString(), etEmail.text.toString(), etPassword.text.toString(), etConfirmPassword.text.toString())
        } else {
            Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToRegisterPetData(name: String, email: String, password: String, numberPets: String) {
        val intent = Intent(this, RegisterPetDataActivity::class.java)
        val bundle = Bundle().apply {
            putString("name", name.trim())
            putString("email", email.trim())
            putString("password", password.trim())
            putString("numberPets", numberPets.trim())
        }
        intent.putExtras(bundle)
        startActivity(intent)
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
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA) -> {
                requestCameraPermission()
            }
            else -> {
                requestCameraPermission()
            }
        }
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), Datos.MY_PERMISSION_REQUEST_CAMERA)
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

    private fun verificarCamposLlenos(vararg campos: EditText): Boolean =
        campos.all { it.text.toString().trim().isNotEmpty() }
}
