package com.example.urumbox.data.model.objetosperdidos

import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

enum class EstadoObjeto {
    PERDIDO,
    ENCONTRADO
}

data class ObjetoPerdido(
    val id: Int = 0,
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
) {
    // Convierte el objeto a Map para guardarlo en Firestore
    fun toMap(): Map<String, Any?> = mapOf(
        "nombre"             to nombre,
        "ubicacion"          to ubicacion,
        "descripcion"        to descripcion,
        "fecha"              to fecha,
        "estado"             to estado.name,
        "categoria"          to categoria,
        "fotoUri"            to fotoUri,
        "latitud"            to latitud,
        "longitud"           to longitud,
        "reportadoPor"       to reportadoPor,
        "nombreReportante"   to nombreReportante,
        "telefonoReportante" to telefonoReportante,
        "correoReportante"   to correoReportante
    )

    companion object {
        // Convierte un documento de Firestore a ObjetoPerdido
        fun fromDocument(doc: DocumentSnapshot): ObjetoPerdido {
            return ObjetoPerdido(
                id                 = doc.id.hashCode(),
                nombre             = doc.getString("nombre") ?: "",
                ubicacion          = doc.getString("ubicacion") ?: "",
                descripcion        = doc.getString("descripcion") ?: "",
                fecha              = doc.getDate("fecha") ?: Date(),
                estado             = EstadoObjeto.valueOf(
                    doc.getString("estado") ?: "PERDIDO"
                ),
                categoria          = doc.getString("categoria") ?: "",
                fotoUri            = doc.getString("fotoUri"),
                latitud            = doc.getDouble("latitud"),
                longitud           = doc.getDouble("longitud"),
                reportadoPor       = doc.getString("reportadoPor"),
                nombreReportante   = doc.getString("nombreReportante") ?: "",
                telefonoReportante = doc.getString("telefonoReportante") ?: "",
                correoReportante   = doc.getString("correoReportante") ?: ""
            )
        }
    }
}
