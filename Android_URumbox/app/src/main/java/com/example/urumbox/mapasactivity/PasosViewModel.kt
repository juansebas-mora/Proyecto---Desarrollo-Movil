package com.example.urumbox.mapasactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.urumbox.data.model.PasoNav

class PasosViewModel : ViewModel() {
    private val _pasoActualIndex = MutableLiveData<Int>(0)
    val pasoActualIndex: LiveData<Int> = _pasoActualIndex

    private val _pasos = MutableLiveData<List<PasoNav>>(emptyList())
    val pasos: LiveData<List<PasoNav>> = _pasos

    fun setPasos(nuevosPasos: List<PasoNav>) {
        _pasos.value = nuevosPasos
    }

    fun avanzarPaso() {
        val actual = _pasoActualIndex.value ?: 0
        val total = _pasos.value?.size ?: 0
        if (actual < total - 1) {
            _pasoActualIndex.value = actual + 1
        }
    }

    fun retrocederPaso() {
        val actual = _pasoActualIndex.value ?: 0
        if (actual > 0) {
            _pasoActualIndex.value = actual - 1
        }
    }

    fun saltarAPaso(index: Int) {
        val total = _pasos.value?.size ?: 0
        if (index in 0 until total) {
            _pasoActualIndex.value = index
        }
    }
}
