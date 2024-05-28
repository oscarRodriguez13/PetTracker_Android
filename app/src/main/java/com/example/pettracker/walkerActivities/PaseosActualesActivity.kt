package com.example.pettracker.walkerActivities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pettracker.R
import com.example.pettracker.adapter.ProgramarPaseoAdapter
import com.example.pettracker.domain.ProgramarPaseoItem

class PaseosActualesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paseos_actuales)

        setupToolbar()

        setupRecyclerView()
    }

    private fun setupToolbar() {
        val historialButton = findViewById<Button>(R.id.buttonOption2)
        historialButton.setOnClickListener {
            navigateToHistorial()
        }

        val settingsButton = findViewById<Button>(R.id.buttonOption3)
        settingsButton.setOnClickListener {
            navigateToSettings()
        }
    }

    private fun navigateToHistorial() {
        val intent = Intent(this, HistorialWalkerActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSettings() {
        val intent = Intent(this, SettingsWalkerActivity::class.java)
        startActivity(intent)
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView1)




    }

    private fun openProfileDetail(profile: ProgramarPaseoItem) {
        val intent = Intent(this, RutaDuenhoActivity::class.java).apply {
            putExtra("nombre", profile.nombre)
            putExtra("duracion", profile.duracion)
            putExtra("cantidad", profile.cantidad)
            putExtra("direccion", "Cl. 45 #8-14")
            putExtra("estado", "en progreso")
        }
        startActivity(intent)
    }
}
