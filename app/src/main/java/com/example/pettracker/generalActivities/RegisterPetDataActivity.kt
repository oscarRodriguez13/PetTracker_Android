package com.example.pettracker.generalActivities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pettracker.R
import com.example.pettracker.apis.NominatimService
import com.example.pettracker.domain.Datos
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class RegisterPetDataActivity : AppCompatActivity(), LocationListener {

    private lateinit var icon: ImageView
    private var photoUserURI: Uri? = null
    private lateinit var etPetName: EditText
    private lateinit var etSpecies: EditText
    private lateinit var etBreed: EditText
    private lateinit var ageSeekBar: SeekBar
    private lateinit var tvAge: TextView
    private lateinit var etDescription: EditText
    private lateinit var buttonNext: Button
    private lateinit var takePictureButton: ImageButton
    private var currentPetIndex = 1
    private var totalPets = 0
    private var name = ""
    private var email = ""
    private var password = ""
    private var edad = 0
    private val petsList = mutableListOf<JSONObject>()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null

    // Firebase
    private var auth: FirebaseAuth = Firebase.auth
    private var user: FirebaseUser? = null

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mGeocoder: Geocoder? = null
    private var geoPoint: GeoPoint? = null
    private var direccionUsuario: String? = null
    private lateinit var locationManager: LocationManager

    private val TAG = "RegisterPetDataActivity"

    private lateinit var nominatimService: NominatimService
    private lateinit var retrofit: Retrofit


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_pet_data)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        handlePermissions()

        mGeocoder = Geocoder(baseContext)

        retrofit = Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        nominatimService = retrofit.create(NominatimService::class.java)

        auth = Firebase.auth
        user = auth.currentUser

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
                photoURI = uri
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
                edad = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Implementar si es necesario
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Implementar si es necesario
            }
        })

        // Obtener el número total de mascotas del Intent
        photoUserURI = Uri.parse(intent.extras?.getString("fotoUsuario").toString())
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
                    registerUser()
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
        values.put(MediaStore.Images.Media.DESCRIPTION, "Tomada desde la aplicación del proyecto")
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
        val petPhotoURI = photoURI?.toString() ?: ""
        val mascotaObject = JSONObject().apply {
            put("nombre", etPetName.text.toString().trim())
            put("especie", etSpecies.text.toString().trim())
            put("raza", etBreed.text.toString().trim())
            put("descripcion", etDescription.text.toString().trim())
            put("edad", edad.toString().trim())
            put("photoURI", petPhotoURI)
        }
        petsList.add(mascotaObject)
        photoURI = null
    }

    private fun clearFields() {
        // Limpiar los campos para la próxima mascota
        etPetName.text.clear()
        etSpecies.text.clear()
        etBreed.text.clear()
        etDescription.text.clear()
        icon.setImageResource(R.drawable.icn_labrador) // Cambia por tu imagen por defecto
        ageSeekBar.progress = 0 // Reiniciar la barra de búsqueda de edad
        tvAge.text = ""
    }

    private fun showToast(message: String) {
        // Muestra un Toast con el mensaje proporcionado
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun registerUser() {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid
                    // Guarda los datos del usuario en la base de datos
                    val database = Firebase.database
                    val ref = database.getReference("Usuarios").child(userId!!)
                    val userData = HashMap<String, Any>()
                    userData["nombre"] = name
                    userData["tipoUsuario"] = "1"
                    userData["latitud"] = geoPoint?.latitude.toString()
                    userData["longitud"] = geoPoint?.longitude.toString()

                    getAddress(geoPoint?.latitude ?: 0.0, geoPoint?.longitude ?: 0.0) { address ->
                        address?.let {
                            userData["direccion"] = address
                            println("direccion $address")


                            ref.setValue(userData)
                                .addOnSuccessListener {
                                    println("Registro del usuario exitoso")
                                    cargarFotoPerfil(userId)
                                    savePets(userId)
                                    navigateToLogin()
                                }.addOnFailureListener { e ->
                                    Snackbar.make(
                                        findViewById(android.R.id.content),
                                        "Error: ${e.message}",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                        } ?: run {
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "No se pudo obtener la dirección",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }


                } else {
                    showToast("Error al registrar usuario")
                }
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun cargarFotoPerfil(userId: String) {
        photoUserURI?.let { uri ->
            val storageRef = Firebase.storage.reference.child("Usuarios/$userId/profile")

            storageRef.putFile(uri)
                .addOnSuccessListener {}
                .addOnFailureListener { e ->
                    println("No funciono la carga de la foto del usuario")
                }
        } ?: run {
            println("URI de foto nula")
        }
    }

    private fun savePets(userId: String) {
        val database = Firebase.database
        val petsRef = database.getReference("Mascotas").child(userId)

        // Iteramos sobre la lista de mascotas
        petsList.forEachIndexed { index, pet ->
            val petId = (index + 1).toString() // ID único de la mascota
            val photoPetURI = Uri.parse(pet.optString("photoURI"))
            val petData = hashMapOf(
                "nombre" to pet.optString("nombre"),
                "especie" to pet.optString("especie"),
                "raza" to pet.optString("raza"),
                "descripcion" to pet.optString("descripcion"),
                "edad" to pet.optString("edad")
            )

            // Verificar si el nodo de la mascota existe
            petsRef.child(petId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Si el nodo no existe, guardamos los datos
                    if (!snapshot.exists()) {
                        // Guardar los datos de la mascota en la base de datos
                        petsRef.child(petId).setValue(petData)
                            .addOnSuccessListener {
                                // Almacenar la imagen de la mascota en el almacenamiento de Firebase
                                val storage = Firebase.storage
                                val storageRef = storage.reference
                                val imageRef = storageRef.child("Mascotas/$userId/$petId")
                                photoPetURI?.let { uri ->
                                    imageRef.putFile(uri)
                                        .addOnSuccessListener { _ ->
                                            if (index == petsList.size - 1) {
                                                // Todas las mascotas se han guardado exitosamente
                                                showToast("Registro completado")
                                                navigateToLogin()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            showToast("Error al subir foto de mascota $petId")
                                            Log.e("UPLOAD_PET_PHOTO", "Error uploading pet photo: ${e.message}", e)
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                showToast("Error al registrar mascota $petId")
                                Log.e("SAVE_PET", "Error saving pet data: ${e.message}", e)
                            }
                    } else {
                        // Manejar el caso donde el nodo ya existe, si es necesario
                        showToast("La mascota $petId ya existe")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Error al verificar existencia de mascota $petId")
                    Log.e("CHECK_PET_EXISTENCE", "Error checking pet existence: ${error.message}", error.toException())
                }
            })
        }
    }

    private fun handlePermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    onLocationChanged(location)
                }
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    Datos.MY_PERMISSION_REQUEST_LOCATION
                )
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    Datos.MY_PERMISSION_REQUEST_LOCATION
                )
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        geoPoint = GeoPoint(location.latitude, location.longitude)

    }

    private fun getAddress(lat: Double, lon: Double, onComplete: (String?) -> Unit) {
        nominatimService.reverseGeocode(lat, lon).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        try {
                            val jsonObject = JSONObject(responseBody)
                            val displayName = jsonObject.getString("display_name")
                            val parts = displayName.split(",")
                            val summarizedAddress = StringBuilder()
                            val relevantParts = listOf("road", "neighbourhood", "suburb")

                            for (part in parts) {
                                val trimmedPart = part.trim()
                                if (relevantParts.any { trimmedPart.contains(it, ignoreCase = true) }) {
                                    if (summarizedAddress.isNotEmpty()) {
                                        summarizedAddress.append(", ")
                                    }
                                    summarizedAddress.append(trimmedPart)
                                }
                            }

                            val finalAddress = if (summarizedAddress.isEmpty()) displayName else summarizedAddress.toString()
                            onComplete(finalAddress)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse JSON", e)
                            onComplete(null)
                        }
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        onComplete(null)
                    }
                } else {
                    Log.e(TAG, "Failed to get address: ${response.errorBody()}")
                    onComplete(null)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "Failed to get address", t)
                onComplete(null)
            }
        })
    }



}
