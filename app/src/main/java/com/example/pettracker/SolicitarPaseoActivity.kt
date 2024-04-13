package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SolicitarPaseoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitar_paseo)

        val historialButton = findViewById<Button>(R.id.buttonOption2)
        historialButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HistorialActivity::class.java
            )
            startActivity(intent)
        }

        findViewById<CardView>(R.id.perfil1).setOnClickListener {
            abrirDetallePerfil()
        }

        findViewById<CardView>(R.id.perfil2).setOnClickListener {
            abrirDetallePerfil()
        }

        findViewById<CardView>(R.id.perfil3).setOnClickListener {
            abrirDetallePerfil()
        }

        findViewById<CardView>(R.id.perfil4).setOnClickListener {
            abrirDetallePerfil()
        }

    }

    private fun abrirDetallePerfil() {
        val intent = Intent(
            this,
            PerfilPaseadorActivity::class.java
        )
        startActivity(intent)
    }

}