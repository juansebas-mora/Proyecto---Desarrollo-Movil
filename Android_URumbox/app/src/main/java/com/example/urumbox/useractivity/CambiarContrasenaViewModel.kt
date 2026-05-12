package com.example.urumbox.useractivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urumbox.data.AuthResult
import com.example.urumbox.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CambiarContrasenaViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _cambioState = MutableStateFlow<AuthResult<Unit>?>(null)
    val cambioState: StateFlow<AuthResult<Unit>?> = _cambioState

    fun cambiarContrasena(actual: String, nueva: String) {
        _cambioState.value = AuthResult.Loading
        viewModelScope.launch {
            _cambioState.value = repository.cambiarContrasena(actual, nueva)
        }
    }
}
