package com.example.pettracker.domain

data class SolicitudItem(
    val solicitudId: String?,
    val hora_inicial: String,
    val hora_final: String,
    val cantidad: String
)