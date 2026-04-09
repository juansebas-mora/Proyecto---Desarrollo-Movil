package com.example.urumbox

data class Notificacion(
    val id: Int,
    val hora: String,
    val tipo: String,              // Incidente, Limpieza, Actividad, Acceso Restringido, Ruta Alternativa
    val nombreReportante: String,  // Quien reportó el aviso
    val fecha: String,
    val zonaAfectada: String,      // Zona del recorrido afectada
    val iconoResId: Int,
    var leida: Boolean = false,
    var eliminada: Boolean = false,
    val descripcion: String = "",        // Descripción del aviso
    val ubicacion: String = "",          // Punto específico dentro de la zona
    val prioridad: String = "Media",     // Alta, Media, Baja
    val horaExpiracion: String = "",     // Hora estimada de resolución
    val rolOrigen: String = "Admin",     // Visitante, Operador, Admin
    var estado: String = "activa",       // activa, pendiente
    val afectaRuta: Boolean = true       // Si bloquea o desvía el trayecto
)