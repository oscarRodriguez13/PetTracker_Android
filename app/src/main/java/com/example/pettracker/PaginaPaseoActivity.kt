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
    }
}