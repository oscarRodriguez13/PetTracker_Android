package com.example.pettracker

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pettracker.Datos.Companion.MY_PERMISSION_REQUEST_CAMERA
import org.json.JSONArray
import org.json.JSONObject
import java.io.*


class RegisterPetData : AppCompatActivity() {

    private lateinit var etPetName: EditText
    private lateinit var etSpecies: EditText
    private lateinit var etBreed: EditText
    private lateinit var etAge: EditText
    private lateinit var etDescription: EditText
    private lateinit var buttonNext: Button

    private var currentPetIndex = 1
    private var totalPets = 0
    private val petsList = mutableListOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_pet_data)

        // Inicializar vistas
        etPetName = findViewById(R.id.etPetName)
        etSpecies = findViewById(R.id.etSpecies)
        etBreed = findViewById(R.id.etBreed)
        etAge = findViewById(R.id.etAge)
        etDescription = findViewById(R.id.etDescription)
        val tvPetProgress = findViewById<TextView>(R.id.tvCurrentPet)
        buttonNext = findViewById(R.id.buttonNext)


        // Obtener el número total de mascotas del Intent
        totalPets = intent.extras?.getString("numberPets")?.toInt() ?: 0

        tvPetProgress.text = "$currentPetIndex/$totalPets"

        // Configurar el botón "Siguiente"
        buttonNext.setOnClickListener {
            // Validar y guardar los datos de la mascota actual
            if (validatePetData()) {
                savePetData()
                // Ir a la siguiente mascota si aún quedan
                if (currentPetIndex < totalPets) {
                    currentPetIndex++
                    tvPetProgress.text = "$currentPetIndex/$totalPets"
                    clearFields()
                } else {
                    // Si no quedan más mascotas, guardar el usuario en el archivo usuarios.json
                    saveUserToJsonFile()
                }
            }
        }

        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                Toast.makeText(this, "Gracias!", Toast.LENGTH_SHORT).show()
                val takePictureButton = findViewById<ImageButton>(R.id.addImageButton)
                takePictureButton.setOnClickListener(View.OnClickListener {
                    val camerai = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(camerai, 123)
                })

            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.CAMERA) -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    MY_PERMISSION_REQUEST_CAMERA)
            }
            else -> {
                // You can directly ask for the permission.
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    MY_PERMISSION_REQUEST_CAMERA)
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //val textV = findViewById<TextView>(R.id.textView3)
        when (requestCode) {
            MY_PERMISSION_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Toast.makeText(this, "Gracias!", Toast.LENGTH_SHORT).show()
                    val takePictureButton = findViewById<ImageButton>(R.id.addImageButton)
                    takePictureButton.setOnClickListener(View.OnClickListener {
                        val camerai = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(camerai, 123)
                    })
                } else {
                    // Explain to the user that the feature is unavailable
                    //textV.text = "PERMISO DENEGADO"
                    //textV.setTextColor(Color.RED)
                    Toast.makeText(this, "Funcionalidades limitadas!", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun validatePetData(): Boolean {
        // Validar que todos los campos estén llenos antes de pasar a la siguiente mascota
        val petName = etPetName.text.toString().trim()
        val species = etSpecies.text.toString().trim()
        val breed = etBreed.text.toString().trim()
        val age = etAge.text.toString().trim()

        if (petName.isEmpty() || species.isEmpty() || breed.isEmpty() || age.isEmpty()) {
            // Si algún campo está vacío, mostrar un mensaje de error y devolver falso
            showToast("Por favor, completa todos los campos")
            return false
        }

        return true
    }

    private fun savePetData() {
        // Guardar los datos de la mascota actual
        val mascotaObject = JSONObject().apply {
            put("nombre", etPetName.text.toString().trim())
            put("especie", etSpecies.text.toString().trim())
            put("raza", etBreed.text.toString().trim())
            put("edad", etAge.text.toString().trim().toIntOrNull() ?: 0)
            put("descripcion", etDescription.text.toString().trim())
        }
        petsList.add(mascotaObject)
    }

    private fun clearFields() {
        // Limpiar los campos para la próxima mascota
        etPetName.text.clear()
        etSpecies.text.clear()
        etBreed.text.clear()
        etAge.text.clear()
        etDescription.text.clear()
    }

    private fun saveUserToJsonFile() {
        val nombre = intent.getStringExtra("nombre") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showToast("Error: Datos de usuario incompletos")
            return
        }

        val usuarioObject = JSONObject().apply {
            put("nombre", nombre)
            put("email", email)
            put("password", password)
            put("mascotas", JSONArray(petsList))
        }

        try {
            val file = File(filesDir, "usuarios.json")
            val jsonArray = if (file.exists()) {
                val jsonString = file.readText()
                JSONArray(jsonString)
            } else {
                JSONArray()
            }

            jsonArray.put(usuarioObject)

            val outputStream = FileOutputStream(file)
            outputStream.use {
                it.write(jsonArray.toString().toByteArray())
            }

            showToast("Usuario registrado exitosamente")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        } catch (e: IOException) {
            e.printStackTrace()
            showToast("Error al guardar usuario")
        }
    }

    private fun showToast(message: String) {
        // Muestra un Toast con el mensaje proporcionado
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
