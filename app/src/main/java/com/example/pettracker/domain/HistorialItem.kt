package com.example.pettracker.domain

data class HistorialItem(
    val nombreMascota: String,
    val fecha: String,
    val nombrePaseador: String,
    val hora_inicial: String,
    val hora_final: String,
    val precio: String,
    val calificacion: Int,
    val comentario: String
)
