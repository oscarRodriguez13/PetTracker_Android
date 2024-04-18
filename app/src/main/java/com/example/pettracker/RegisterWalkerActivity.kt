package com.example.pettracker

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer

class RegisterWalkerActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etExperience: EditText
    private lateinit var buttonRegister: Button
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_walker)

        initializeViews()
        setupButtons()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etExperience = findViewById(R.id.etExperience)
        buttonRegister = findViewById(R.id.button)
    }

    private fun setupButtons() {
        buttonRegister.setOnClickListener {
            handleRegistration()
        }

        setupImagePickers()
    }

    private fun handleRegistration() {
        writeJSONObject()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun writeJSONObject() {
        val usuariosArray = readJSONObject()

        val userObject = JSONObject().apply {
            put("nombre", etName.text.toString())
            put("email", etEmail.text.toString())
            put("password", etPassword.text.toString())
            put("tipoUsuario", "2")
        }

        usuariosArray.put(userObject)

        var output: Writer?
        val filename = "usuarios.json"

        try {
            val file = File(baseContext.getExternalFilesDir(null), filename)
            output = BufferedWriter(FileWriter(file))
            output.write("{\"usuarios\": $usuariosArray}")
            output.close()
            Toast.makeText(applicationContext, "Usuario guardado", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("USER", "Error al guardar el usuario: ${e.message}", e)
        }
    }

    private fun readJSONObject(): JSONArray {
        var jsonArray = JSONArray()
        val filename = "usuarios.json"

        try {
            val file = File(baseContext.getExternalFilesDir(null), filename)
            if (file.exists()) {
                val content = file.readText()
                jsonArray = JSONObject(content).getJSONArray("usuarios")
            }
        } catch (e: Exception) {
            Log.e("READ_JSON", "Error al leer el archivo JSON: ${e.message}", e)
        }

        return jsonArray
    }

    private fun setupImagePickers() {
        val perfilView = findViewById<CircleImageView>(R.id.icn_paseador)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                perfilView.setImageURI(uri)
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoURI?.let {
                    perfilView.setImageURI(it)
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
