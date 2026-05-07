package com.example.urumbox.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Notificacion(
    @DocumentId
    val id: String = "",
    val hora: String = "",
    val tipo: String = "",
    val nombreReportante: String = "",
    val fecha: String = "",
    val zonaAfectada: String = "",
    @get:Exclude
    var iconoResId: Int = 0,
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
