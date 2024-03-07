package com.example.pettracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class HomeWalkerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_walker)

        val buttonAceptar = findViewById<Button>(R.id.Aceptar)

    }
}