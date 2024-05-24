package com.example.pettracker.customerActivities

import android.os.Bundle
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.pettracker.R

class DetallesHistorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_historial)

        val extras = intent.extras

        setTextView(R.id.nombreMascotaView, extras?.getString("nombreMascota"))
        setTextView(R.id.fechaView, extras?.getString("fecha"))
        setTextView(R.id.nombrePaseadorView, extras?.getString("nombrePaseador"))
        setTextView(R.id.horaInicialView, extras?.getString("hora_inicial"))
        setTextView(R.id.horaFinalView, extras?.getString("hora_final"))
        setTextView(R.id.precioView, extras?.getString("precio"))

        setRatingBar(R.id.calificacionView, extras?.getInt("calificacion", 0)?.toFloat())
        setTextView(R.id.comentarioView, extras?.getString("comentario"))
    }

    private fun setTextView(id: Int, text: String?) {
        findViewById<TextView>(id).text = text
    }

    private fun setRatingBar(id: Int, rating: Float?) {
        val ratingBar = findViewById<RatingBar>(id)
        ratingBar.rating = rating ?: 0f
        ratingBar.isClickable = false
        ratingBar.isFocusable = false
        ratingBar.stepSize = 1f
        ratingBar.numStars = 5
        ratingBar.isEnabled = false
    }
}
