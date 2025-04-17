package com.example.pettracker.domain

data class SolicitudPaseo(
    val estado: String? = null,
    val horaFin: String? = null,
    val horaInicio: String? = null,
    val petIds: List<String> = listOf(),
    val uidDueño: String? = null,
    val uidPaseador: String? = null
)