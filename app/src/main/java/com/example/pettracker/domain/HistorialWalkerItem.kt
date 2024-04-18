package com.example.pettracker.domain

data class HistorialWalkerItem(
    val fecha: String,
    val nombreDuenho: String,
    val hora_inicial: String,
    val hora_final: String,
    val estado: String,
    val precio: String,
)