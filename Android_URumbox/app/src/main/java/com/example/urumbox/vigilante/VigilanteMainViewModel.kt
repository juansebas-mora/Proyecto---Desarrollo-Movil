package com.example.urumbox.vigilante

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VigilanteMainViewModel : ViewModel() {

    private val _zonaError = MutableLiveData(false)
    val zonaError: LiveData<Boolean> = _zonaError

    private val _navigateToScanner = MutableLiveData<String?>()
    val navigateToScanner: LiveData<String?> = _navigateToScanner

    fun onZonaSelected() {
        _zonaError.value = false
    }

    fun onCardQrClicked(selectedZona: String?) {
        if (selectedZona.isNullOrBlank()) {
            _zonaError.value = true
        } else {
            _zonaError.value = false
            _navigateToScanner.value = selectedZona
        }
    }

    fun onNavigationConsumed() {
        _navigateToScanner.value = null
    }
}
