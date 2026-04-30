package com.example.urumbox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

sealed class AccessRequestEvent {
    object ShowConfirmationDialog : AccessRequestEvent()
    object NavigateToConsult : AccessRequestEvent()
}

class AccessRequestViewModel : ViewModel() {

    private val _nombresError = MutableLiveData<String?>()
    val nombresError: LiveData<String?> = _nombresError

    private val _apellidosError = MutableLiveData<String?>()
    val apellidosError: LiveData<String?> = _apellidosError

    private val _correoError = MutableLiveData<String?>()
    val correoError: LiveData<String?> = _correoError

    private val _documentoError = MutableLiveData<String?>()
    val documentoError: LiveData<String?> = _documentoError

    private val _fechaError = MutableLiveData<String?>()
    val fechaError: LiveData<String?> = _fechaError

    private val _uiEvent = MutableLiveData<AccessRequestEvent?>()
    val uiEvent: LiveData<AccessRequestEvent?> = _uiEvent

    private val namePattern = Regex("^[\\p{L} ]+$")
    private val emailPattern = Regex("^[a-zA-Z]+\\.[a-zA-Z]+@urosario\\.edu\\.co$")

    fun validateNombres(value: String, showEmptyError: Boolean = false) {
        _nombresError.value = when {
            value.isBlank() -> if (showEmptyError) "Campo obligatorio" else null
            !namePattern.matches(value) -> "Solo se permiten letras. No se admiten números ni caracteres especiales."
            else -> null
        }
    }

    fun validateApellidos(value: String, showEmptyError: Boolean = false) {
        _apellidosError.value = when {
            value.isBlank() -> if (showEmptyError) "Campo obligatorio" else null
            !namePattern.matches(value) -> "Solo se permiten letras. No se admiten números ni caracteres especiales."
            else -> null
        }
    }

    fun validateCorreo(value: String, showEmptyError: Boolean = false) {
        _correoError.value = when {
            value.isBlank() -> if (showEmptyError) "Campo obligatorio" else null
            !emailPattern.matches(value) -> "El correo debe tener el formato nombre.apellido@urosario.edu.co"
            else -> null
        }
    }

    fun validateDocumento(value: String, showEmptyError: Boolean = false) {
        _documentoError.value = when {
            value.isBlank() -> if (showEmptyError) "Campo obligatorio" else null
            else -> null
        }
    }

    fun validateFecha(value: String, showEmptyError: Boolean = false) {
        _fechaError.value = when {
            value.isBlank() || value.length < 10 -> if (showEmptyError) "Campo obligatorio" else null
            else -> {
                val parts = value.split("/")
                val day = parts.getOrNull(0)?.toIntOrNull()
                val month = parts.getOrNull(1)?.toIntOrNull()
                when {
                    day == null || month == null -> "Formato inválido"
                    day !in 1..31 -> "El día debe estar entre 01 y 31"
                    month !in 1..12 -> "El mes debe estar entre 01 y 12"
                    else -> null
                }
            }
        }
    }

    fun onRegisterClicked(
        nombres: String,
        apellidos: String,
        correo: String,
        documento: String,
        fecha: String
    ) {
        validateNombres(nombres, showEmptyError = true)
        validateApellidos(apellidos, showEmptyError = true)
        validateCorreo(correo, showEmptyError = true)
        validateDocumento(documento, showEmptyError = true)
        validateFecha(fecha, showEmptyError = true)

        val hasErrors = listOf(
            _nombresError.value,
            _apellidosError.value,
            _correoError.value,
            _documentoError.value,
            _fechaError.value
        ).any { it != null }

        if (!hasErrors) {
            _uiEvent.value = AccessRequestEvent.ShowConfirmationDialog
        }
    }

    fun onNavigateToConsult() {
        _uiEvent.value = AccessRequestEvent.NavigateToConsult
    }

    fun onEventConsumed() {
        _uiEvent.value = null
    }

    fun clearErrors() {
        _nombresError.value = null
        _apellidosError.value = null
        _correoError.value = null
        _documentoError.value = null
        _fechaError.value = null
    }
}
