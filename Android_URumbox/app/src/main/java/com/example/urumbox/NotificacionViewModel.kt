package com.example.urumbox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NotificacionViewModel : ViewModel() {

    private val repository = NotificacionRepository()

    val notificaciones: LiveData<List<Notificacion>> = repository
        .obtenerNotificaciones()
        .asLiveData()

    private val _estadoCreacion = MutableLiveData<Result<Unit>>()
    val estadoCreacion: LiveData<Result<Unit>> = _estadoCreacion

    init {
        cargarNotificaciones()
    }

    fun cargarNotificaciones() {
        // notificaciones already streams via asLiveData() when observers are active
    }

    fun crearNotificacion(notificacion: Notificacion) {
        viewModelScope.launch {
            repository.crearNotificacion(notificacion).collect { result ->
                _estadoCreacion.value = result
            }
        }
    }
}