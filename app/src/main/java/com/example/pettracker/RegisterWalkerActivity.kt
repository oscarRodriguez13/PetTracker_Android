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
import android.widget.ImageView
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

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etExperience = findViewById(R.id.etExperience)
        buttonRegister = findViewById(R.id.button)

        buttonRegister.setOnClickListener {
            writeJSONObject()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val perfilView = findViewById<CircleImageView>(R.id.icn_paseador)

        // Preparar el lanzador para el resultado de selección de imagen.
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                perfilView.setImageURI(uri)
            }
        }

        // Preparar el lanzador para el resultado de tomar foto.
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoURI?.let {
                    perfilView.setImageURI(it)
                }
            }
        }

        findViewById<ImageButton>(R.id.agregarFotoView).setOnClickListener {
            // Intent explícito para seleccionar una imagen de la galería
            imagePickerLauncher.launch("image/*")
        }

        findViewById<ImageButton>(R.id.tomarFotoView).setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    //Lanzamos la camara
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
    }

    private fun writeJSONObject() {
        val usuariosArray = readJSONObject()

        // Crear el objeto de usuario con sus datos
        val userObject = JSONObject().apply {
            put("nombre", etName.text.toString())
            put("email", etEmail.text.toString())
            put("password", etPassword.text.toString())
            put("tipoUsuario", "2") // Puedes definir el tipo de usuario como quieras
        }
        // Añadir el usuario al array de usuarios
        usuariosArray.put(userObject)

        var output: Writer?
        val filename = "usuarios.json"

        try {
            val file = File(baseContext.getExternalFilesDir(null), filename)
            Log.i("USER", "Ubicacion de archivo: $file")
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
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Funcionalidades limitadas!", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

}
