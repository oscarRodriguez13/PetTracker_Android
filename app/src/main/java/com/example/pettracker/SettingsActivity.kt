package com.example.pettracker

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pettracker.domain.Datos
import com.example.pettracker.domain.Profile
import com.example.pettracker.domain.SolicitudPaseoAdapter
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Arrays

class SettingsActivity : AppCompatActivity() {

    private var isNotified = false
    private lateinit var fotoPaseador: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        fotoPaseador = findViewById(R.id.icn_perfil)

        setupImagePickers()

        setupButtons()

        setupRecyclerView()
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
    }

    private fun setupButtons() {
        findViewById<ImageButton>(R.id.agregarFotoView).setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        findViewById<ImageButton>(R.id.tomarFotoView).setOnClickListener {
            handleCameraPermission()
        }

        findViewById<Button>(R.id.buttonOption2).setOnClickListener {
            navigateToHistorial()
        }

        findViewById<Button>(R.id.buttonOption1).setOnClickListener {
            navigateToHome()
        }

        findViewById<CircleImageView>(R.id.icn_logout).setOnClickListener {
            navigateToLogin()
        }

        findViewById<CircleImageView>(R.id.icn_notificacion).setOnClickListener {
            toggleNotification()
        }
    }

    private fun handleCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
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

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val profiles = Arrays.asList(
            Profile(R.drawable.img_perro1, "Tony", "Labrador"),
            Profile(R.drawable.img_perro2, "Alaska", "Lobo siberiano"),
            Profile(R.drawable.img_perro3, "Firulais", "Beagle")
        )

        val adapter = SolicitudPaseoAdapter(profiles) { profile ->
            abrirDetallePerfil(profile)
        }
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
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

    private fun navigateToHistorial() {
        val intent = Intent(applicationContext, HistorialActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToHome() {
        val intent = Intent(applicationContext, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun toggleNotification() {
        val notifyButton = findViewById<CircleImageView>(R.id.icn_notificacion)
        if (isNotified) {
            notifyButton.setImageResource(R.drawable.icn_notificacion_inactiva)
        } else {
            notifyButton.setImageResource(R.drawable.icn_notificacion)
        }
        isNotified = !isNotified
    }

    private fun abrirDetallePerfil(profile: Profile) {
        val intent = Intent(this, DetallesMascotaActivity::class.java)
        intent.putExtra("profileName", profile.name)
        intent.putExtra("profilePrice", profile.price)
        startActivity(intent)
    }
}
