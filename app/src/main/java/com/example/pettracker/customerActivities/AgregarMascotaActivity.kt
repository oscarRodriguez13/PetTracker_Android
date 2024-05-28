package com.example.pettracker.customerActivities

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pettracker.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AgregarMascotaActivity : AppCompatActivity() {
    private lateinit var etPetName: EditText
    private lateinit var etSpecies: EditText
    private lateinit var etBreed: EditText
    private lateinit var ageSeekBar: SeekBar
    private lateinit var tvAge: TextView
    private lateinit var etDescription: EditText
    private lateinit var buttonAddPet: Button
    private lateinit var icon: ImageView
    private lateinit var addImageButton: ImageButton
    private var photoURI: Uri? = null

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    // Firebase
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = Firebase.database
    private val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_mascota)

        etPetName = findViewById(R.id.etPetName)
        etSpecies = findViewById(R.id.etSpecies)
        etBreed = findViewById(R.id.etBreed)
        etDescription = findViewById(R.id.etDescription)
        ageSeekBar = findViewById(R.id.ageSeekBar)
        tvAge = findViewById(R.id.tvAge)
        buttonAddPet = findViewById(R.id.buttonAddPet)
        icon = findViewById(R.id.icon)
        addImageButton = findViewById(R.id.addImageButton)

        // Inicializar lanzadores de actividad para seleccionar imagen y tomar foto
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                icon.setImageURI(uri)
                photoURI = uri
            }
        }

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

        icon.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        addImageButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), 100
                )
            }
        }

        buttonAddPet.setOnClickListener {
            if (validatePetData()) {
                savePetData()
            }
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Nueva foto")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Foto tomada desde la app")
        photoURI = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        photoURI?.let { takePictureLauncher.launch(it) }
    }

    private fun validatePetData(): Boolean {
        val petName = etPetName.text.toString().trim()
        val species = etSpecies.text.toString().trim()
        val breed = etBreed.text.toString().trim()

        if (petName.isEmpty() || species.isEmpty() || breed.isEmpty()) {
            showToast("Por favor, completa todos los campos")
            return false
        }

        return true
    }

    private fun savePetData() {
        val userId = auth.currentUser?.uid ?: return
        val petName = etPetName.text.toString().trim()
        val species = etSpecies.text.toString().trim()
        val breed = etBreed.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val age = ageSeekBar.progress.toString()

        val petsRef = database.getReference("Mascotas").child(userId)
        val petId = petsRef.push().key ?: return

        val petData = hashMapOf(
            "nombre" to petName,
            "especie" to species,
            "raza" to breed,
            "descripcion" to description,
            "edad" to age
        )

        if (photoURI != null) {
            val imageRef = storage.reference.child("Mascotas/$userId/$petId")
            imageRef.putFile(photoURI!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        petData["photoURI"] = uri.toString()
                        petsRef.child(petId).setValue(petData)
                            .addOnSuccessListener {
                                showToast("Mascota registrada exitosamente")
                                clearFields()
                                navigateToSettings()
                            }
                            .addOnFailureListener { e ->
                                showToast("Error al registrar mascota: ${e.message}")
                            }
                    }
                }
        } else {
            petsRef.child(petId).setValue(petData)
                .addOnSuccessListener {
                    showToast("Mascota registrada exitosamente")
                    clearFields()
                    navigateToSettings()
                }
                .addOnFailureListener { e ->
                    showToast("Error al registrar mascota: ${e.message}")
                }
        }
    }

    private fun navigateToSettings() {
        val intent = Intent(applicationContext, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun clearFields() {
        etPetName.text.clear()
        etSpecies.text.clear()
        etBreed.text.clear()
        etDescription.text.clear()
        ageSeekBar.progress = 0
        tvAge.text = ""
        icon.setImageResource(R.drawable.icn_labrador)
        photoURI = null
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
