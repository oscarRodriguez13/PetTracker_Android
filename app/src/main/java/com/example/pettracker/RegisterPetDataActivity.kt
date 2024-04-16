package com.example.pettracker

import android.annotation.SuppressLint
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
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pettracker.domain.Datos
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.Writer

class RegisterPetDataActivity : AppCompatActivity() {

    private lateinit var icon: ImageView
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
    private var name = ""
    private var email = ""
    private var password = ""
    private val petsList = mutableListOf<JSONObject>()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_pet_data)

        setup()
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
        icon = findViewById(R.id.icon)

        // Preparar el lanzador para el resultado de selección de imagen.
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                icon.setImageURI(uri)
            }
        }

        // Preparar el lanzador para el resultado de tomar foto.
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoURI?.let {
                    icon.setImageURI(it)
                }
            }
        }

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
        name = intent.extras?.getString("name").toString()
        email = intent.extras?.getString("email").toString()
        password = intent.extras?.getString("password").toString()

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
                    writeJSONObject()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        icon.setOnClickListener {
            // Intent explícito para seleccionar una imagen de la galería
            imagePickerLauncher.launch("image/*")
        }

        // Configurar el botón de tomar foto
        takePictureButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    //Lanzamos la camara
                    openCamera()
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, android.Manifest.permission.CAMERA) -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.CAMERA),
                        Datos.MY_PERMISSION_REQUEST_CAMERA)
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

    private fun showToast(message: String) {
        // Muestra un Toast con el mensaje proporcionado
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun writeJSONObject() {
        val usuariosArray = readJSONObject()

        // Crear el objeto de usuario con sus datos
        val userObject = JSONObject().apply {
            put("nombre", name)
            put("email", email)
            put("password", password)
            put("tipoUsuario", "1") // Puedes definir el tipo de usuario como quieras
        }

        // Añadir la lista de mascotas al usuario
        val mascotasArray = JSONArray()
        for (pet in petsList) {
            mascotasArray.put(pet)
        }
        userObject.put("mascotas", mascotasArray)

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
            Toast.makeText(applicationContext, "Usuario y mascotas guardados", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("USER", "Error al guardar el usuario y mascotas: ${e.message}", e)
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
}
