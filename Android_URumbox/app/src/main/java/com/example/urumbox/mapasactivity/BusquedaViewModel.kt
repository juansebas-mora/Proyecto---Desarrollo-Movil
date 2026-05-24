package com.example.urumbox.mapasactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.urumbox.data.model.Ruta
import com.example.urumbox.data.repository.RutaRepository

class BusquedaViewModel : ViewModel() {
    private val repository = RutaRepository()
    
    private val _allRutas = MutableLiveData<List<Ruta>>(emptyList())
    
    private val _filteredRutas = MutableLiveData<List<Ruta>>(emptyList())
    val filteredRutas: LiveData<List<Ruta>> = _filteredRutas

    private var currentQuery: String = ""

    init {
        cargarRutas()
    }

    fun cargarRutas() {
        repository.obtenerTodasLasRutas { result ->
            val rutas = result.getOrDefault(emptyList())
            _allRutas.postValue(rutas)
            filtrarRutas(rutas, currentQuery)
        }
    }

    fun setQuery(newQuery: String) {
        currentQuery = newQuery
        filtrarRutas(_allRutas.value ?: emptyList(), newQuery)
    }

    private fun filtrarRutas(rutas: List<Ruta>, query: String) {
        if (query.isBlank()) {
            _filteredRutas.postValue(rutas)
        } else {
            val filtered = rutas.filter {
                it.destino.contains(query, ignoreCase = true) ||
                it.nombre.contains(query, ignoreCase = true) ||
                it.origen.contains(query, ignoreCase = true)
            }
            _filteredRutas.postValue(filtered)
        }
    }
}
