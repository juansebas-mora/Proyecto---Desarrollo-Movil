package com.example.urumbox.data.model

data class AccessRequest(
    val id: String = "",
    val nombres: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val documento: String = "",
    val fecha: String = "",
    val userId: String = "",
    val estado: String = "pendiente"
)
