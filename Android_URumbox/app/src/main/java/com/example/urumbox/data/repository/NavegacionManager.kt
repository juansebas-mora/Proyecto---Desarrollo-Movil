package com.example.urumbox.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.urumbox.data.model.Ruta

object NavegacionManager {
    private val _rutaActiva = MutableLiveData<Ruta?>()
    val rutaActiva: LiveData<Ruta?> = _rutaActiva

    private val _pasoActualIndex = MutableLiveData<Int>(0)
    val pasoActualIndex: LiveData<Int> = _pasoActualIndex

    fun iniciarNavegacion(ruta: Ruta) {
        _rutaActiva.value = ruta
        _pasoActualIndex.value = 0
    }

    fun establecerPasoIndex(index: Int) {
        val totalPasos = _rutaActiva.value?.pasos?.size ?: 0
        if (index in 0 until totalPasos) {
            _pasoActualIndex.value = index
        }
    }

    fun avanzarPaso(): Boolean {
        val actual = _pasoActualIndex.value ?: 0
        val total = _rutaActiva.value?.pasos?.size ?: 0
        if (actual < total - 1) {
            _pasoActualIndex.value = actual + 1
            return true
        }
        return false
    }

    fun retrocederPaso(): Boolean {
        val actual = _pasoActualIndex.value ?: 0
        if (actual > 0) {
            _pasoActualIndex.value = actual - 1
            return true
        }
        return false
    }
}
