package com.example.pettracker.walkerActivities

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pettracker.R
import com.example.pettracker.domain.Datos
import com.example.pettracker.domain.Profile
import com.example.pettracker.adapter.SolicitudPaseoAdapter
import com.example.pettracker.customerActivities.DetallesMascotaActivity
import com.example.pettracker.generalActivities.LoginActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Arrays

class SettingsWalkerActivity: AppCompatActivity() {

    private var isNotified = false
    private lateinit var fotoPaseador: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_walker)

        fotoPaseador = findViewById(R.id.icn_perfil)


        // Preparar el lanzador para el resultado de selección de imagen.
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                fotoPaseador.setImageURI(uri)
            }
        }

        // Preparar el lanzador para el resultado de tomar foto.
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoURI?.let {
                    fotoPaseador.setImageURI(it)
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

        val userEmail = intent.getStringExtra("EMAIL")

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val profiles = Arrays.asList(
            Profile("a", null, "Anónimo", "Muy buen paseador, perfecto como siempre"),
            Profile("b", null, "Anónimo", "Paseo regular, Rocky volvió inquieto"),
            Profile("a", null, "Anónimo", "Buen paseo, pero Toby necesitaba más agua")
        )

        // Usar un adaptador personalizado con funcionalidad de clic
        val adapter = SolicitudPaseoAdapter(profiles) { profile ->
            abrirDetallePerfil(profile)
        }
        recyclerView.adapter = adapter

        // Establecer el LinearLayoutManager para vertical
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val historialButton = findViewById<Button>(R.id.buttonOption2)
        historialButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HistorialWalkerActivity::class.java
            )
            startActivity(intent)
        }

        val buttonPaseos = findViewById<Button>(R.id.buttonOption1)
        buttonPaseos.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HomeWalkerActivity::class.java
            )
            startActivity(intent)
        }

        val logOutButton = findViewById<CircleImageView>(R.id.icn_logout)
        logOutButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                LoginActivity::class.java
            )
            startActivity(intent)
        }

        val notifyButton = findViewById<CircleImageView>(R.id.icn_notificacion)
        notifyButton.setOnClickListener {
            if (isNotified) {
                // Cambiar a la imagen original
                notifyButton.setImageResource(R.drawable.icn_notificacion_inactiva)
            } else {
                // Cambiar a la nueva imagen
                notifyButton.setImageResource(R.drawable.icn_notificacion)
            }

            // Actualizar el estado del botón
            isNotified = !isNotified
        }

        val ratingBar = findViewById<RatingBar>(R.id.calificacionView)
        ratingBar.isClickable = false
        ratingBar.isFocusable = false
        ratingBar.stepSize = 1f // Definir un paso de calificación de 1 para evitar calificaciones parciales
        ratingBar.numStars = 5 // Definir el número máximo de estrellas
        ratingBar.rating = 4f
        ratingBar.isEnabled = false

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

    private fun abrirDetallePerfil(profile: Profile) {
        val intent = Intent(this, DetallesMascotaActivity::class.java)
        // Aquí puedes agregar datos adicionales al intent si es necesario
        intent.putExtra("profileName", profile.name)
        intent.putExtra("profilePrice", profile.price)
        startActivity(intent)
    }
}