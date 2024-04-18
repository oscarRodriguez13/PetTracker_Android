package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PerfilDuenhoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_duenho)

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
