package com.example.urumbox.notificationactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.urumbox.data.model.Notificacion
import com.example.urumbox.data.repository.NotificacionRepository
import com.google.firebase.firestore.ListenerRegistration

class NotificacionViewModel : ViewModel() {

    private val repository = NotificacionRepository()

    private val _notificaciones = MutableLiveData<List<Notificacion>>(emptyList())
    val notificaciones: LiveData<List<Notificacion>> = _notificaciones

    private val _estadoCreacion = MutableLiveData<Result<Unit>?>()
    val estadoCreacion: LiveData<Result<Unit>?> = _estadoCreacion

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var listenerRegistration: ListenerRegistration? = null

    init {
        cargarNotificaciones()
    }

    fun cargarNotificaciones() {
        listenerRegistration = repository.obtenerNotificaciones { result ->
            result.fold(
                onSuccess = { _notificaciones.value = it },
                onFailure = { e -> _error.value = e.message ?: "Error al cargar notificaciones" }
            )
        }
    }

    fun crearNotificacion(notificacion: Notificacion) {
        repository.crearNotificacion(notificacion) { result ->
            _estadoCreacion.value = result
        }
    }

    fun marcarLeida(id: String, estadoActual: String) {
        val nuevoEstado = if (estadoActual == "pendiente") "activa" else estadoActual
        repository.marcarLeida(id, nuevoEstado) { result ->
            result.onFailure { e -> _error.value = e.message ?: "Error al actualizar notificación" }
        }
    }

    fun archivarNotificacion(id: String) {
        repository.archivarNotificacion(id) { result ->
            result.onFailure { e -> _error.value = e.message ?: "Error al archivar notificación" }
        }
    }

    fun restaurarNotificacion(id: String) {
        repository.restaurarNotificacion(id) { result ->
            result.onFailure { e -> _error.value = e.message ?: "Error al restaurar notificación" }
        }
    }

    fun eliminarNotificacion(id: String) {
        repository.eliminarNotificacion(id) { result ->
            result.onFailure { e -> _error.value = e.message ?: "Error al eliminar notificación" }
        }
    }

    fun onEstadoCreacionConsumed() {
        _estadoCreacion.value = null
    }

    fun onErrorConsumed() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
