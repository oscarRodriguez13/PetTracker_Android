package com.example.pettracker.domain

data class ProgramarPaseoItem(
    val solicitudId: String?,
    val usuarioUid: String,
    val image: String,
    val nombre: String,
    val hora_inicial: String,
    val hora_final: String,
    val cantidad: String
)