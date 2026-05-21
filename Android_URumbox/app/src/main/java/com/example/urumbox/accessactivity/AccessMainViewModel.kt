package com.example.urumbox.accessactivity

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urumbox.data.model.AccessRequest
import com.example.urumbox.data.model.UserQrData
import com.example.urumbox.data.repository.AccessRequestRepository
import com.example.urumbox.data.repository.QrException
import com.example.urumbox.data.repository.QrRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// region AccessMain



sealed class AccessMainEvent {
    object ShowAddVisitorDialog : AccessMainEvent()
    object NavigateToRequest : AccessMainEvent()
    object NavigateToConsult : AccessMainEvent()
}

class AccessMainViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _uiEvent = MutableLiveData<AccessMainEvent?>()
    val uiEvent: LiveData<AccessMainEvent?> = _uiEvent

    fun loadUserInfo() {
        val user = auth.currentUser ?: return
        db.collection("usuarios").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val nombreCompleto = doc.getString("nombreCompleto")
                    ?: "${doc.getString("nombre") ?: ""} ${doc.getString("apellido") ?: ""}".trim()
                val correo = doc.getString("correo")
                    ?: doc.getString("email")
                    ?: user.email
                    ?: ""
                _userName.value = nombreCompleto
                _userEmail.value = correo
            }
    }

    fun onAddVisitorClicked() {
        _uiEvent.value = AccessMainEvent.ShowAddVisitorDialog
    }

    fun onRegisterRequestClicked() {
        _uiEvent.value = AccessMainEvent.NavigateToRequest
    }

    fun onConsultRequestsClicked() {
        _uiEvent.value = AccessMainEvent.NavigateToConsult
    }

    fun onEventConsumed() {
        _uiEvent.value = null
    }
}

// endregion

// region AccessHistory

class AccessHistoryViewModel : ViewModel()

// endregion

// region AccessQr

class AccessQrViewModel : ViewModel() {

    private val repo = QrRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _qrBitmap = MutableLiveData<Bitmap?>()
    val qrBitmap: LiveData<Bitmap?> = _qrBitmap

    private val _validDate = MutableLiveData<String>()
    val validDate: LiveData<String> = _validDate

    private val _loadError = MutableLiveData<String?>()
    val loadError: LiveData<String?> = _loadError

    fun loadAndGenerateQr() {
        val uid = auth.currentUser?.uid ?: run {
            _loadError.value = "Usuario no autenticado"
            return
        }
        viewModelScope.launch {
            try {
                val now = Calendar.getInstance().time
                val todayDate = SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(now)
                val displayDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
                _validDate.value = "Válido para: $displayDate"
                val token = repo.getOrCreateQrToken(uid, todayDate)
                val size = 512
                val bitMatrix = QRCodeWriter().encode(token, BarcodeFormat.QR_CODE, size, size)
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                _qrBitmap.value = bitmap
            } catch (e: Exception) {
                _loadError.value = "Error al generar el código QR"
            }
        }
    }
}

// endregion

// region AccessRequest

sealed class AccessRequestEvent {
    object NavigateToConsult : AccessRequestEvent()
}

class AccessRequestViewModel : ViewModel() {

    private val repository = AccessRequestRepository()

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

    private val _registrationResult = MutableLiveData<Result<Unit>?>()
    val registrationResult: LiveData<Result<Unit>?> = _registrationResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

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
            _isLoading.value = true
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val request = AccessRequest(
                nombres = nombres,
                apellidos = apellidos,
                correo = correo,
                documento = documento,
                fecha = fecha,
                userId = uid
            )
            repository.registerAccessRequest(request) { result ->
                _isLoading.value = false
                _registrationResult.value = result
            }
        }
    }

    fun onNavigateToConsult() {
        _uiEvent.value = AccessRequestEvent.NavigateToConsult
    }

    fun onEventConsumed() {
        _uiEvent.value = null
    }

    fun onRegistrationResultConsumed() {
        _registrationResult.value = null
    }

    fun clearErrors() {
        _nombresError.value = null
        _apellidosError.value = null
        _correoError.value = null
        _documentoError.value = null
        _fechaError.value = null
    }
}

// endregion

// region AccessRequestConsult

class AccessRequestConsultViewModel : ViewModel() {

    private val repository = AccessRequestRepository()

    private val _accessRequests = MutableLiveData<List<AccessRequest>>(emptyList())
    val accessRequests: LiveData<List<AccessRequest>> = _accessRequests

    private val _loadError = MutableLiveData<String?>()
    val loadError: LiveData<String?> = _loadError

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadAccessRequests() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _isLoading.value = true
        repository.getAccessRequests(uid) { result ->
            _isLoading.value = false
            result.fold(
                onSuccess = { requests ->
                    _accessRequests.value = requests
                    _loadError.value = null
                },
                onFailure = { e ->
                    _loadError.value = e.message ?: "Error al cargar las solicitudes"
                }
            )
        }
    }

    fun onErrorConsumed() {
        _loadError.value = null
    }
}

// endregion

// region QrScanner

sealed class QrValidationResult {
    data class Success(val userData: UserQrData) : QrValidationResult()
    data class Error(val type: QrException) : QrValidationResult()
}

class QrScannerViewModel : ViewModel() {

    private val repo = QrRepository()

    private val _validationResult = MutableLiveData<QrValidationResult?>()
    val validationResult: LiveData<QrValidationResult?> = _validationResult

    fun validateQrContent(scannedContent: String) {
        val todayDate = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
            .format(Calendar.getInstance().time)
        viewModelScope.launch {
            try {
                val userData = repo.validateQrToken(scannedContent, todayDate)
                _validationResult.value = QrValidationResult.Success(userData)
            } catch (e: QrException) {
                _validationResult.value = QrValidationResult.Error(e)
            }
        }
    }

    fun onResultConsumed() {
        _validationResult.value = null
    }
}

// endregion
