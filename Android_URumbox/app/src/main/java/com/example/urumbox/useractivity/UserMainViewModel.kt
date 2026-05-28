package com.example.urumbox.useractivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urumbox.data.AuthResult
import com.example.urumbox.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// region CambiarContrasenaViewModel

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

// endregion

// region RegistroViewModel

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
        fechaNacimiento: String,
        documentoIdentidad: String
    ) {
        _registroState.value = AuthResult.Loading
        viewModelScope.launch {
            _registroState.value = repository.registrarUsuario(email, password, nombre, apellido, telefono, fechaNacimiento, documentoIdentidad)
        }
    }
}

// endregion

// region LoginViewModel

sealed class LoginDestination {
    object Main : LoginDestination()
    object Vigilante : LoginDestination()
}

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginState = MutableStateFlow<AuthResult<Unit>?>(null)
    val loginState: StateFlow<AuthResult<Unit>?> = _loginState

    private val _destination = MutableStateFlow<LoginDestination?>(null)
    val destination: StateFlow<LoginDestination?> = _destination

    fun login(email: String, password: String) {
        _loginState.value = AuthResult.Loading
        viewModelScope.launch {
            _loginState.value = repository.loginUsuario(email, password)
        }
    }

    fun resolveDestination(uid: String) {
        viewModelScope.launch {
            val rol = repository.getUserRole(uid)
            _destination.value = if (rol == "Vigilante") LoginDestination.Vigilante else LoginDestination.Main
        }
    }

    fun onDestinationConsumed() {
        _destination.value = null
    }
}

// endregion
