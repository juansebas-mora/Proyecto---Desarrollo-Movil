package com.example.urumbox.data.repository

import com.example.urumbox.data.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registrarUsuario(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        telefono: String,
        fechaNacimiento: String
    ): AuthResult<Unit> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: return AuthResult.Error("No se pudo obtener el usuario")
        val usuario = email.substringBefore("@")
        firestore.collection("usuarios").document(uid).set(
            mapOf(
                "nombreCompleto" to "$nombre $apellido",
                "usuario" to usuario,
                "correo" to email,
                "rol" to "Estudiante",
                "telefono" to telefono,
                "fechaNacimiento" to fechaNacimiento
            )
        ).await()
        AuthResult.Success(Unit)
    } catch (e: FirebaseAuthException) {
        AuthResult.Error(mapAuthError(e.errorCode))
    } catch (e: Exception) {
        AuthResult.Error(e.message ?: "Error desconocido")
    }

    suspend fun loginUsuario(email: String, password: String): AuthResult<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        AuthResult.Success(Unit)
    } catch (e: FirebaseAuthException) {
        AuthResult.Error(mapAuthError(e.errorCode))
    } catch (e: Exception) {
        AuthResult.Error(e.message ?: "Error desconocido")
    }

    suspend fun cambiarContrasena(contrasenaActual: String, contrasenaNueva: String): AuthResult<Unit> = try {
        val user = auth.currentUser ?: return AuthResult.Error("No hay sesión activa")
        val email = user.email ?: return AuthResult.Error("No se pudo obtener el correo del usuario")
        val credential = EmailAuthProvider.getCredential(email, contrasenaActual)
        user.reauthenticate(credential).await()
        user.updatePassword(contrasenaNueva).await()
        AuthResult.Success(Unit)
    } catch (e: FirebaseAuthException) {
        AuthResult.Error(mapAuthError(e.errorCode))
    } catch (e: Exception) {
        AuthResult.Error(e.message ?: "Error desconocido")
    }

    fun cerrarSesion() = auth.signOut()

    fun sesionActiva() = auth.currentUser != null

    private fun mapAuthError(code: String): String = when (code) {
        "ERROR_INVALID_EMAIL" -> "Correo electrónico inválido"
        "ERROR_EMAIL_ALREADY_IN_USE" -> "El correo ya está registrado"
        "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres"
        "ERROR_USER_NOT_FOUND" -> "No existe cuenta con este correo"
        "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta"
        "ERROR_INVALID_CREDENTIAL" -> "Correo o contraseña incorrectos"
        "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada"
        "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Intenta más tarde"
        "ERROR_NETWORK_REQUEST_FAILED" -> "Error de conexión. Verifica tu internet"
        else -> "Error de autenticación. Intenta de nuevo"
    }
}
