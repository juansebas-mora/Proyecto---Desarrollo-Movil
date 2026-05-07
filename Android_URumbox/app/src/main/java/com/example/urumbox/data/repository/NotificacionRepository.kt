package com.example.urumbox.data.repository

import com.example.urumbox.R
import com.example.urumbox.data.model.Notificacion
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NotificacionRepository {

    private val coleccion = Firebase.firestore.collection("notificaciones")

    fun obtenerNotificaciones(onResult: (Result<List<Notificacion>>) -> Unit): ListenerRegistration {
        return coleccion.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onResult(Result.failure(error))
                return@addSnapshotListener
            }
            val lista = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Notificacion::class.java)?.apply {
                    iconoResId = iconoPorTipo(tipo)
                }
            } ?: emptyList()
            onResult(Result.success(lista))
        }
    }

    fun crearNotificacion(notificacion: Notificacion, onResult: (Result<Unit>) -> Unit) {
        coleccion.add(notificacion)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    private fun iconoPorTipo(tipo: String): Int = when (tipo) {
        "Incidente"          -> R.drawable.ic_warning_white
        "Limpieza"           -> R.drawable.ic_restaurar_white
        "Actividad"          -> R.drawable.ic_group_white
        "Acceso Restringido" -> R.drawable.ic_lock_white
        "Ruta Alternativa"   -> R.drawable.ic_help_circle_white
        else                 -> R.drawable.ic_warning_white
    }
}
