package com.example.urumbox

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NotificacionRepository {

    private val coleccion = FirebaseFirestore.getInstance().collection("notificaciones")

    fun obtenerNotificaciones(): Flow<List<Notificacion>> = callbackFlow {
        val listener = coleccion.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val lista = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Notificacion::class.java)?.apply {
                    iconoResId = iconoPorTipo(tipo)
                }
            } ?: emptyList()
            trySend(lista)
        }
        awaitClose { listener.remove() }
    }

    fun crearNotificacion(notificacion: Notificacion): Flow<Result<Unit>> = callbackFlow {
        coleccion.add(notificacion)
            .addOnSuccessListener {
                trySend(Result.success(Unit))
                close()
            }
            .addOnFailureListener { error ->
                trySend(Result.failure(error))
                close()
            }
        awaitClose()
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