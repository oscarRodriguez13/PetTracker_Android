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
import java.util.Arrays

class PaseosProgramadosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paseos_programados)

        setupBarraHerramientas()

        // Configuración del RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // Usar un adaptador personalizado con funcionalidad de clic

    }


    private fun setupBarraHerramientas() {
        // Botón para ir al historial
        val historialButton = findViewById<Button>(R.id.buttonOption2)
        historialButton.setOnClickListener {
            val intent = Intent(
                this@PaseosProgramadosActivity,
                HistorialWalkerActivity::class.java
            )
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.buttonOption3)
        settingsButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                SettingsWalkerActivity::class.java
            )
            startActivity(intent)
        }
    }
    private fun abrirDetallePerfil(profile: ProgramarPaseoItem) {
        val intent = Intent(this, RutaDuenhoActivity::class.java)
        // Aquí puedes agregar datos adicionales al intent si es necesario
        intent.putExtra("nombre", profile.nombre)
        intent.putExtra("duracion", profile.duracion)
        intent.putExtra("cantidad", profile.cantidad)
        intent.putExtra("direccion", "Cl. 45 #8-14")
        intent.putExtra("estado", "sin empezar")
        startActivity(intent)
    }
}