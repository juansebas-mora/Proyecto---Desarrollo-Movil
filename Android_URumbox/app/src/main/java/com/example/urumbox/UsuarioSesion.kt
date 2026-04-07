package com.example.urumbox

object UsuarioSesion {
    var nombre: String = ""
    var rol: String = "Visitante"  // Visitante, Operador, Admin

    val estaLogueado: Boolean get() = nombre.isNotEmpty()

    fun cerrarSesion() {
        nombre = ""
        rol = "Visitante"
    }
}
