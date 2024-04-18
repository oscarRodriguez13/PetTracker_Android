package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PerfilPaseadorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_paseador)

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
            navigateToPaginaPaseo()
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
}
