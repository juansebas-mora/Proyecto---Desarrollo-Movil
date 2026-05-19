package com.example.urumbox.mapasactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.urumbox.data.model.Ruta
import com.example.urumbox.data.repository.RutaRepository

class MapaViewModel : ViewModel() {
    private val repository = RutaRepository()

    private val _ruta = MutableLiveData<Ruta?>()
    val ruta: LiveData<Ruta?> = _ruta

    private val _cargando = MutableLiveData<Boolean>(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun cargarRuta(idRuta: String) {
        _cargando.value = true
        _error.value = null
        repository.obtenerRuta(idRuta) { result ->
            _cargando.postValue(false)
            result.fold(
                onSuccess = { _ruta.postValue(it) },
                onFailure = { _error.postValue(it.message ?: "Error al obtener la ruta") }
            )
        }
    }
}
