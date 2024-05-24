package com.example.pettracker.customerActivities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pettracker.R

class FinalizarPaseoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finalizar_paseo)

        setupPagarButton()
        setupRatingBar()
        setupHistorialButton()
        setupSettingsButton()
    }

    private fun setupPagarButton() {
        findViewById<Button>(R.id.button2).setOnClickListener {
            startNewActivity(HomeActivity::class.java)
        }
    }

    private fun setupRatingBar() {
        findViewById<RatingBar>(R.id.ratingBar).setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                Toast.makeText(this, "Calificación seleccionada: $rating", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupHistorialButton() {
        findViewById<Button>(R.id.buttonOption2).setOnClickListener {
            startNewActivity(HistorialActivity::class.java)
        }
    }

    private fun setupSettingsButton() {
        findViewById<Button>(R.id.buttonOption3).setOnClickListener {
            startNewActivity(SettingsActivity::class.java)
        }
    }

    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(applicationContext, activityClass)
        startActivity(intent)
    }
}
