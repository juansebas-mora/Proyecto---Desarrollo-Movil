package com.example.urumbox.data.repository

import com.example.urumbox.data.model.AccessRequest
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class AccessRequestRepository {

    private val collection = Firebase.firestore.collection("access_requests")

    fun registerAccessRequest(request: AccessRequest, onResult: (Result<Unit>) -> Unit) {
        collection.add(request)
            .addOnSuccessListener { docRef ->
                docRef.update("id", docRef.id)
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { e -> onResult(Result.failure(e)) }
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    fun getAccessRequests(userId: String, onResult: (Result<List<AccessRequest>>) -> Unit) {
        collection.whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val requests = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(AccessRequest::class.java)
                }
                onResult(Result.success(requests))
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    fun getAllAccessRequests(onResult: (Result<List<AccessRequest>>) -> Unit) {
        collection.get()
            .addOnSuccessListener { snapshot ->
                val requests = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(AccessRequest::class.java)
                }
                onResult(Result.success(requests))
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    fun updateAccessRequestStatus(id: String, estado: String, onResult: (Result<Unit>) -> Unit) {
        collection.document(id)
            .update("estado", estado)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }
}
