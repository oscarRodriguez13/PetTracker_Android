package com.example.pettracker.generalActivities

import android.Manifest
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
import com.example.pettracker.R
import com.example.pettracker.apis.NominatimService
import com.example.pettracker.domain.Datos
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class RegisterWalkerActivity : AppCompatActivity(), LocationListener {

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

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mGeocoder: Geocoder? = null
    private var geoPoint: GeoPoint? = null
    private lateinit var locationManager: LocationManager

    private val TAG = "RegisterWalkerActivity"

    private lateinit var nominatimService: NominatimService
    private lateinit var retrofit: Retrofit


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_walker)

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
                        val database = Firebase.database
                        val ref = database.getReference("Usuarios").child(userId!!)
                        val userData = HashMap<String, Any>()
                        userData["nombre"] = name
                        userData["experiencia"] = experience
                        userData["latitud"] = geoPoint?.latitude.toString()
                        userData["longitud"] = geoPoint?.longitude.toString()
                        userData["comentarios"] = {}
                        userData["tipoUsuario"] = "2"

                        getAddress(geoPoint?.latitude ?: 0.0, geoPoint?.longitude ?: 0.0) { address ->
                            address?.let {
                                userData["direccion"] = address
                                println("direccion $address")
                                ref.setValue(userData)
                                    .addOnSuccessListener {
                                        cargarFotoPerfil(userId)
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
                        Snackbar.make(findViewById(android.R.id.content), "Error: ${task.exception?.message}", Snackbar.LENGTH_LONG).show()
                    }
                }

        }
    }

    private fun cargarFotoPerfil(userId: String) {
        photoURI?.let { uri ->
            val storageRef = Firebase.storage.reference.child("Usuarios/$userId/profile")

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
