package com.example.urumbox.data.repository

import com.example.urumbox.data.model.UserQrData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class QrRepository {

    private val db = FirebaseFirestore.getInstance()

    fun generateToken(uid: String, dateStr: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("${uid}_${dateStr}".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun getOrCreateQrToken(uid: String, todayDate: String): String {
        val docRef = db.collection("usuarios").document(uid)
        val doc = docRef.get().await()
        val savedDate = doc.getString("qrDate") ?: ""
        val savedToken = doc.getString("qrToken") ?: ""
        if (savedDate == todayDate && savedToken.isNotEmpty()) return savedToken
        val newToken = generateToken(uid, todayDate)
        docRef.update(mapOf("qrToken" to newToken, "qrDate" to todayDate)).await()
        return newToken
    }

    suspend fun validateQrToken(scannedContent: String, todayDate: String): UserQrData {
        if (!scannedContent.matches(Regex("[0-9a-f]{64}"))) throw QrException.InvalidFormat
        val snapshot = db.collection("usuarios")
            .whereEqualTo("qrToken", scannedContent)
            .get().await()
        if (snapshot.isEmpty) throw QrException.NotFound
        val doc = snapshot.documents.first()
        if (doc.getString("qrDate") != todayDate) throw QrException.Expired
        return UserQrData(uid = doc.id, nombre = doc.getString("nombreCompleto") ?: "")
    }
}

sealed class QrException : Exception() {
    object InvalidFormat : QrException()
    object NotFound : QrException()
    object Expired : QrException()
}
