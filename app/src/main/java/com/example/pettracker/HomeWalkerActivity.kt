package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeWalkerActivity : AppCompatActivity() {

    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_walker)

        userEmail = intent.getStringExtra("EMAIL") ?: ""

        setupButtons()
        setupBarraTareas()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnBuscarPaseo).setOnClickListener {
            startActivity(Intent(this, ProgramarPaseoActivity::class.java))
        }

        findViewById<Button>(R.id.btnPaseosProgramados).setOnClickListener {
            startActivity(Intent(this, PaseosProgramadosActivity::class.java))
        }

        findViewById<Button>(R.id.btnPaseosActuales).setOnClickListener {
            startActivity(Intent(this, PaseosActualesActivity::class.java))
        }
    }

    private fun setupBarraTareas() {
        findViewById<Button>(R.id.buttonOption2).setOnClickListener {
            startActivity(Intent(this, HistorialWalkerActivity::class.java))
        }

        findViewById<Button>(R.id.buttonOption3).setOnClickListener {
            startActivity(Intent(this, SettingsWalkerActivity::class.java).apply {
                putExtra("EMAIL", userEmail)
            })
        }
    }
}
