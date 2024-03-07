package com.example.pettracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class FinalizarPaseoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finalizar_paseo)

        val pagarButton = findViewById<Button>(R.id.button2)
        pagarButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HomeActivity::class.java
            )
            startActivity(intent)
        }
    }
}