package com.example.pettracker.domain

data class Pet(
    val id: Int,
    val name: String,
    val age: String,  // asumiendo que la edad es en meses
    val breed: String,
    val description: String
)
