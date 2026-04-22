package com.example.urumbox

data class Notificacion(
    val id: Int,
    val hora: String,
    val tipo: String,
    val nombreReportante: String,
    val fecha: String,
    val zonaAfectada: String,
    val iconoResId: Int,
    var leida: Boolean = false,
    var eliminada: Boolean = false,
    val descripcion: String = "",
    val ubicacion: String = "",
    val prioridad: String = "Media",
    val horaExpiracion: String = "",
    val rolOrigen: String = "Admin",
    var estado: String = "activa",
    val afectaRuta: Boolean = true
)
