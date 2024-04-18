package com.example.pettracker

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetallesHistorialWalkerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_historial_walker)


        val fecha = intent.getStringExtra("fecha")
        val nombre = intent.getStringExtra("nombreDuenho")
        val horaInicial = intent.getStringExtra("hora_inicial")
        val horaFinal = intent.getStringExtra("hora_final")
        val precio = intent.getStringExtra("precio")
        val estado = intent.getStringExtra("estado")

        // Aquí puedes asignar estos valores a las vistas de tu layout
        findViewById<TextView>(R.id.nombreDueño).text = "Usuario: " + nombre
        findViewById<TextView>(R.id.fechaView).text = "Fecha: " + fecha
        findViewById<TextView>(R.id.estadoView).text = "Estado: " + estado
        findViewById<TextView>(R.id.horaInicialView).text = "Hora inicial: " + horaInicial
        findViewById<TextView>(R.id.horaFinalView).text = "Hora final: " + horaFinal
        findViewById<TextView>(R.id.precioView).text = "Precio: " + precio
    }
}