package com.example.pettracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class PaginaPaseoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagina_paseo)

        val mascotaButton = findViewById<Button>(R.id.ir_mascota)
        mascotaButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                BuscarMascotaActivity::class.java
            )
            startActivity(intent)
        }
        val historialButton = findViewById<Button>(R.id.buttonOption2)
        historialButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HistorialActivity::class.java
            )
            startActivity(intent)
        }

        val terminarButton = findViewById<Button>(R.id.bt_terminar)
        terminarButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                FinalizarPaseoActivity::class.java
            )
            startActivity(intent)
        }
    }
}