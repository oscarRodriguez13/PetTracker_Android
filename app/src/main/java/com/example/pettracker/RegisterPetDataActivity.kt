package com.example.pettracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pettracker.domain.Datos
import com.example.pettracker.domain.Datos.Companion.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RegisterPetDataActivity : AppCompatActivity() {

    private lateinit var etPetName: EditText
    private lateinit var etSpecies: EditText
    private lateinit var etBreed: EditText
    private lateinit var ageSeekBar : SeekBar
    private lateinit var tvAge : TextView
    private lateinit var etDescription: EditText
    private lateinit var buttonNext: Button
    private lateinit var takePictureButton: ImageButton
    private var currentPetIndex = 1
    private var totalPets = 0
    private val petsList = mutableListOf<JSONObject>()



    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                showToast("Funcionalidad limitada: permiso denegado")
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Aquí puedes manejar la imagen capturada
            } else {
                showToast("Error al capturar la imagen")
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_pet_data)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no ha sido otorgado, solicítalo
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        }else{
            setup()
        }

    }

    private fun setup() {
        // Inicializar vistas
        etPetName = findViewById(R.id.etPetName)
        etSpecies = findViewById(R.id.etSpecies)
        etBreed = findViewById(R.id.etBreed)
        etDescription = findViewById(R.id.etDescription)
        val tvPetProgress = findViewById<TextView>(R.id.tvCurrentPet)
        buttonNext = findViewById(R.id.buttonNext)
        takePictureButton = findViewById(R.id.addImageButton)
        ageSeekBar = findViewById(R.id.ageSeekBar)
        tvAge = findViewById(R.id.tvAge)

        ageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvAge.text = "Edad: $progress meses"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Implementar si es necesario
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Implementar si es necesario
            }
        })

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

        // Configurar el botón de tomar foto
        takePictureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val camerai = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(camerai)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                setup()
            }else {
                Toast.makeText(this, "Funcionalidad limitada: permiso denegado", Toast.LENGTH_SHORT).show()

            }
        }
    }


    private fun validatePetData(): Boolean {
        // Validar que todos los campos estén llenos antes de pasar a la siguiente mascota
        val petName = etPetName.text.toString().trim()
        val species = etSpecies.text.toString().trim()
        val breed = etBreed.text.toString().trim()

        if (petName.isEmpty() || species.isEmpty() || breed.isEmpty()) {
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
            put("descripcion", etDescription.text.toString().trim())
        }
        petsList.add(mascotaObject)
    }

    private fun clearFields() {
        // Limpiar los campos para la próxima mascota
        etPetName.text.clear()
        etSpecies.text.clear()
        etBreed.text.clear()
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

            // Agregar el objeto de usuario al arreglo JSON
            jsonArray.put(usuarioObject)

            // Escribir el arreglo JSON actualizado en el archivo
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
