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
import com.example.pettracker.domain.Profile
import com.example.pettracker.domain.SolicitudPaseoAdapter
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Arrays

class SettingsActivity : AppCompatActivity() {

    private var isNotified = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val userEmail = intent.getStringExtra("EMAIL")

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val profiles = Arrays.asList(
            Profile(R.drawable.img_perro1, "Tony", "Labrador"),
            Profile(R.drawable.img_perro2, "Alaska", "Lobo siberiano"),
            Profile(R.drawable.img_perro3, "Firulais", "Beagle")
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
                HistorialActivity::class.java
            )
            startActivity(intent)
        }

        val buttonPaseos = findViewById<Button>(R.id.buttonOption1)
        buttonPaseos.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HomeActivity::class.java
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
    }

    private fun abrirDetallePerfil(profile: Profile) {
        val intent = Intent(this, DetallesMascotaActivity::class.java)
        // Aquí puedes agregar datos adicionales al intent si es necesario
        intent.putExtra("profileName", profile.name)
        intent.putExtra("profilePrice", profile.price)
        startActivity(intent)
    }
}