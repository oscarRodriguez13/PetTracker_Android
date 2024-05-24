package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pettracker.domain.Profile
import com.example.pettracker.domain.SolicitudPaseoAdapter
import java.util.Arrays

class SolicitarPaseoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitar_paseo)

        setupBarraHerramientas()

        // Configuración del RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val profiles = Arrays.asList(
            Profile(R.drawable.img_perfil3, "Olivia", "$ 45.000"),
            Profile(R.drawable.img_perfil2, "Tony Stark", "$ 53.000"),
            Profile(R.drawable.img_perfil1, "Charles", "$ 41.000")
        )

        // Usar un adaptador personalizado con funcionalidad de clic
        val adapter = SolicitudPaseoAdapter(profiles) { profile ->
            abrirDetallePerfil(profile)
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
                applicationContext,
                HistorialActivity::class.java
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

        val cancelButton = findViewById<Button>(R.id.btnCancelar)
        cancelButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HomeActivity::class.java
            )
            startActivity(intent)
        }
    }
    private fun abrirDetallePerfil(profile: Profile) {
        val intent = Intent(this, PerfilPaseadorActivity::class.java)
        // Aquí puedes agregar datos adicionales al intent si es necesario
        intent.putExtra("profileName", profile.name)
        intent.putExtra("profilePrice", profile.price)
        startActivity(intent)
    }
}
