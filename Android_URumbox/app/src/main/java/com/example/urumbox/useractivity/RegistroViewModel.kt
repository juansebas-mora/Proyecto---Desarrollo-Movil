package com.example.urumbox.useractivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urumbox.data.AuthResult
import com.example.urumbox.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegistroViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _registroState = MutableStateFlow<AuthResult<Unit>?>(null)
    val registroState: StateFlow<AuthResult<Unit>?> = _registroState

    fun registrar(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        telefono: String,
        fechaNacimiento: String
    ) {
        _registroState.value = AuthResult.Loading
        viewModelScope.launch {
            _registroState.value = repository.registrarUsuario(email, password, nombre, apellido, telefono, fechaNacimiento)
        }
    }
}
