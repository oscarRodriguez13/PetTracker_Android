package com.example.pettracker

import android.os.Bundle
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetallesHistorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_historial)

        val nombreMascota = intent.getStringExtra("nombreMascota")
        val fecha = intent.getStringExtra("fecha")
        val nombrePaseador = intent.getStringExtra("nombrePaseador")
        val horaInicial = intent.getStringExtra("hora_inicial")
        val horaFinal = intent.getStringExtra("hora_final")
        val precio = intent.getStringExtra("precio")
        val calificacion = intent.getIntExtra("calificacion", 0) // Cambia getIntExtra a getFloatExtra
        val comentario = intent.getStringExtra("comentario")

        // Aquí puedes asignar estos valores a las vistas de tu layout
        findViewById<TextView>(R.id.nombreMascotaView).text = nombreMascota
        findViewById<TextView>(R.id.fechaView).text = fecha
        findViewById<TextView>(R.id.nombrePaseadorView).text = nombrePaseador
        findViewById<TextView>(R.id.horaInicialView).text = horaInicial
        findViewById<TextView>(R.id.horaFinalView).text = horaFinal
        findViewById<TextView>(R.id.precioView).text = precio

        val ratingBar = findViewById<RatingBar>(R.id.calificacionView)
        ratingBar.rating = calificacion.toFloat()
        ratingBar.isClickable = false
        ratingBar.isFocusable = false
        ratingBar.stepSize = 1f // Definir un paso de calificación de 1 para evitar calificaciones parciales
        ratingBar.numStars = 5 // Definir el número máximo de estrellas
        ratingBar.rating = calificacion.toFloat() // Establecer el valor de la calificación
        ratingBar.isEnabled = false

        findViewById<TextView>(R.id.comentarioView).text = comentario
    }
}
