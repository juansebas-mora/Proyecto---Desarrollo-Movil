package com.example.urumbox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.urumbox.data.model.Notificacion
import com.example.urumbox.data.model.objetosperdidos.ObjetoPerdido
import com.example.urumbox.data.model.objetosperdidos.EstadoObjeto
import com.example.urumbox.data.repository.NotificacionRepository
import com.example.urumbox.data.repository.objetosperdidos.ObjetoRepository
import com.google.firebase.firestore.ListenerRegistration

class MainViewModel : ViewModel() {
    private val repository = NotificacionRepository()
    private val objetoRepository = ObjetoRepository()
    private var listenerRegistration: ListenerRegistration? = null

    private val _latestNotifications = MutableLiveData<List<Notificacion>>(emptyList())
    val latestNotifications: LiveData<List<Notificacion>> = _latestNotifications

    private val _latestLostObjects = MutableLiveData<List<ObjetoPerdido>>(emptyList())
    val latestLostObjects: LiveData<List<ObjetoPerdido>> = _latestLostObjects

    init {
        cargarNotificaciones()
        cargarObjetosPerdidos()
    }

    private fun cargarNotificaciones() {
        listenerRegistration = repository.obtenerNotificaciones { result ->
            result.fold(
                onSuccess = { allList ->
                    val activeList = allList.filter { !it.eliminada && it.estado == "activa" }
                    val latest = activeList.take(2)
                    _latestNotifications.value = latest
                },
                onFailure = {
                    _latestNotifications.value = emptyList()
                }
            )
        }
    }

    fun cargarObjetosPerdidos() {
        objetoRepository.obtenerObjetos(
            onExito = { list ->
                val lost = list.filter { it.estado == EstadoObjeto.PERDIDO }.take(2)
                _latestLostObjects.value = lost
            },
            onError = {
                _latestLostObjects.value = emptyList()
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}


