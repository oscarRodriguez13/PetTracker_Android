package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pettracker.domain.ProgramarPaseoAdapter
import com.example.pettracker.domain.ProgramarPaseoItem
import java.util.Arrays

class PaseosProgramadosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paseos_programados)

        setupBarraHerramientas()

        // Configuración del RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val profiles = Arrays.asList(
            ProgramarPaseoItem(R.drawable.img_perfil3, "Sofía Hernández", "Duración: 30 min", "Mascotas: 1"),
            ProgramarPaseoItem(R.drawable.img_perfil2, "Marta Pérez", "Duración: 30 min", "Mascotas: 1"),
            ProgramarPaseoItem(R.drawable.img_perfil1, "Pedro Jimenez", "Duración: 30 min", "Mascotas: 1")
        )

        // Usar un adaptador personalizado con funcionalidad de clic
        val adapter = ProgramarPaseoAdapter(profiles) { ProgramarPaseoItem ->
            abrirDetallePerfil(ProgramarPaseoItem)
        }
        recyclerView.adapter = adapter

        // Establecer el LinearLayoutManager para vertical
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
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
                SettingsActivity::class.java
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