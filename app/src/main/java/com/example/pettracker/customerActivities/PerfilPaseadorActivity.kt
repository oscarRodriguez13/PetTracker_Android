package com.example.pettracker.customerActivities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.pettracker.R
import com.example.pettracker.domain.Datos
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PerfilPaseadorActivity : AppCompatActivity() {

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var paseadorUid: String? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var nombrePaseador: TextView
    private lateinit var descripcion: TextView
    private lateinit var fotoPerfilPaseador: ImageView
    private var solicitudId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_paseador)

        paseadorUid = intent.getStringExtra("profileUid")
        solicitudId = intent.getStringExtra("solicitudId")

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        database = FirebaseDatabase.getInstance()

        // Inicializar vistas
        nombrePaseador = findViewById(R.id.nombre_paseador)
        descripcion = findViewById(R.id.descripcion)
        fotoPerfilPaseador = findViewById(R.id.foto_perfil_paseador)

        // Cargar datos desde Firebase
        if (paseadorUid != null) {
            loadProfileData(paseadorUid!!)
        } else {
            Toast.makeText(this, "UID no proporcionado", Toast.LENGTH_SHORT).show()
        }

        setupButtons()
    }

    private fun setupButtons() {
        val historialButton = findViewById<Button>(R.id.buttonOption2)
        historialButton.setOnClickListener {
            navigateToHistorial()
        }

        val settingsButton = findViewById<Button>(R.id.buttonOption3)
        settingsButton.setOnClickListener {
            navigateToSettings()
        }

        val contratarButton = findViewById<Button>(R.id.btnContratar)
        contratarButton.setOnClickListener {
            handlePermissions()
        }
    }

    private fun handlePermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    navigateToPaginaPaseo()
                    updatePaseoRequest()
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
                handlePermissions()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Datos.MY_PERMISSION_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    navigateToPaginaPaseo()
                    updatePaseoRequest()
                } else {
                    Toast.makeText(this, "Los permisos son necesarios para acceder a la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
            }
        }
    }

    private fun navigateToHistorial() {
        val intent = Intent(this, HistorialActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToPaginaPaseo() {
        val intent = Intent(this, PaginaPaseoActivity::class.java)
        startActivity(intent)
    }

    private fun updatePaseoRequest() {
        if (solicitudId != null && paseadorUid != null) {
            val solicitudRef = database.getReference("SolicitudesPaseo").child(solicitudId!!)
            solicitudRef.child("estado").setValue("asignado")
            solicitudRef.child("uidPaseador").setValue(paseadorUid)
            Toast.makeText(this, "Solicitud actualizada con éxito", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al actualizar la solicitud", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileData(userId: String) {
        val userRef = database.getReference("Usuarios").child(userId)
        val profileRef = Firebase.storage.reference.child("Usuarios").child(userId).child("profile")
        profileRef.downloadUrl.addOnSuccessListener { uri ->
            val profileImageUrl = uri.toString()

            // Cargar la imagen usando Glide
            Glide.with(this@PerfilPaseadorActivity) // Utiliza el contexto del itemView
                .load(profileImageUrl) // Utiliza la URL de la imagen
                .fitCenter()
                .into(fotoPerfilPaseador)

        }.addOnFailureListener {
            fotoPerfilPaseador.setImageResource(R.drawable.icn_foto_perfil)
        }

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("nombre").getValue(String::class.java) ?: "Nombre no disponible"
                val descripcionText = snapshot.child("experiencia").getValue(String::class.java) ?: "Descripción no disponible"

                nombrePaseador.text = nombre
                descripcion.text = descripcionText
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PerfilPaseadorActivity, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
