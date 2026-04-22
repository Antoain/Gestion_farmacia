package com.example.gestion_farmacia

data class Medicamento(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val precio: Double = 0.0,
    val stock: Int = 0,
    val descripcion: String = ""
)