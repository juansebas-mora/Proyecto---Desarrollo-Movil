package com.example.urumbox

import java.util.Date

enum class EstadoObjeto {
    PERDIDO,
    ENCONTRADO
}

data class ObjetoPerdido(
    val id: Int,
    val nombre: String,
    val ubicacion: String,
    val descripcion: String,
    val fecha: Date,
    val estado: EstadoObjeto,
    val imagenResId: Int? = null,
    val categoria: String = "",
    val fotoUri: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val reportadoPor: String? = null,
    val nombreReportante: String = "",
    val telefonoReportante: String = "",
    val correoReportante: String = ""
)
