package com.example.urumbox.data.repository

import com.example.urumbox.data.model.AccessHistoryItem
import com.example.urumbox.data.model.UserQrData
import com.google.firebase.firestore.FieldValue
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

    suspend fun saveAccessRecord(userId: String, zona: String, registradoPor: String): Result<Unit> {
        return try {
            val data = mapOf(
                "userId" to userId,
                "zona" to zona,
                "registradoPor" to registradoPor,
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection("historial_de_accesos").add(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentAccessHistory(userId: String, limit: Long = 5): Result<List<AccessHistoryItem>> {
        return try {
            val snapshot = db.collection("historial_de_accesos")
                .whereEqualTo("userId", userId)
                .get().await()
            val items = snapshot.documents
                .map { doc ->
                    AccessHistoryItem(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        zona = doc.getString("zona") ?: "",
                        timestamp = doc.getTimestamp("timestamp"),
                        registradoPor = doc.getString("registradoPor") ?: ""
                    )
                }
                .sortedByDescending { it.timestamp?.seconds }
                .take(limit.toInt())
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFullAccessHistory(userId: String): Result<List<AccessHistoryItem>> {
        return try {
            val snapshot = db.collection("historial_de_accesos")
                .whereEqualTo("userId", userId)
                .get().await()
            val items = snapshot.documents
                .map { doc ->
                    AccessHistoryItem(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        zona = doc.getString("zona") ?: "",
                        timestamp = doc.getTimestamp("timestamp"),
                        registradoPor = doc.getString("registradoPor") ?: ""
                    )
                }
                .sortedByDescending { it.timestamp?.seconds }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

sealed class QrException : Exception() {
    object InvalidFormat : QrException()
    object NotFound : QrException()
    object Expired : QrException()
}
