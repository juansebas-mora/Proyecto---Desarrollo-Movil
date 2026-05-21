package com.example.urumbox.data.model

data class Coordenada(
    val x: Float = 0f,
    val y: Float = 0f,
    val piso: Int = 1
)

data class PasoNav(
    val num_paso: Int = 1,
    val titulo: String = "",
    val descripcion: String = "",
    val icono: String = "ic_walk",
    val pasos_requeridos: Int = 10,
    val piso: Int = 1,
    val x: Float = 0f,
    val y: Float = 0f
)

data class Ruta(
    val id_ruta: String = "",
    val nombre: String = "",
    val origen: String = "",
    val destino: String = "",
    val coordenadas: List<Coordenada> = emptyList(),
    val pasos: List<PasoNav> = emptyList()
)
