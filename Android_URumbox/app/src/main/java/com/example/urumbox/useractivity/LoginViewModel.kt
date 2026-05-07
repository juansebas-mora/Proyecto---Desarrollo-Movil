package com.example.urumbox.useractivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urumbox.data.AuthResult
import com.example.urumbox.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginState = MutableStateFlow<AuthResult<Unit>?>(null)
    val loginState: StateFlow<AuthResult<Unit>?> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = AuthResult.Loading
        viewModelScope.launch {
            _loginState.value = repository.loginUsuario(email, password)
        }
    }
}
