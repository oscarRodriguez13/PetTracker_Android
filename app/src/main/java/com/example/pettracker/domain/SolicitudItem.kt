package com.example.pettracker.domain

data class SolicitudItem(
    val solicitudId: String?,
    val estado: String?,
    val uidPaseador: String?,
    val hora_inicial: String,
    val hora_final: String,
    val cantidad: String
)