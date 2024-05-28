package com.example.pettracker.walkerActivities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.pettracker.R
import com.example.pettracker.customerActivities.SettingsActivity

class PerfilDuenhoActivity : AppCompatActivity() {

    private lateinit var nombreDuenoText: TextView
    private lateinit var direccionText: TextView

    private lateinit var mascotasList: ListView

    private lateinit var fotoPerfilImageView: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_duenho)

        nombreDuenoText = findViewById(R.id.nombre_paseador)
        direccionText = findViewById(R.id.direccion)
        mascotasList = findViewById(R.id.listaMascotas)


        val intent = intent
        val uid = intent.getStringExtra("uid")




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

        val aceptarButton = findViewById<Button>(R.id.btnAceptar)
        aceptarButton.setOnClickListener {
            navigateToHomeWalker()
        }
    }

    private fun navigateToHistorial() {
        val intent = Intent(this, HistorialWalkerActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToHomeWalker() {
        val intent = Intent(this, HomeWalkerActivity::class.java)
        startActivity(intent)
    }
}



