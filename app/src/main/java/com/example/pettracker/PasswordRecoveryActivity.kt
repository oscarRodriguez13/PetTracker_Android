package com.example.pettracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class PasswordRecoveryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recovery)

        val button_confirmar = findViewById<Button>(R.id.button_confirmar)
        button_confirmar.setOnClickListener {
            val intent = Intent(
                applicationContext,
                LoginActivity::class.java
            )
            startActivity(intent)
        }
    }
}