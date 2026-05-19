package com.example.urumbox.data.repository

import com.example.urumbox.data.model.Ruta
import com.google.firebase.firestore.FirebaseFirestore

class RutaRepository {
    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("rutas")

    fun obtenerRuta(idRuta: String, callback: (Result<Ruta>) -> Unit) {
        coleccion.document(idRuta).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val ruta = document.toObject(Ruta::class.java)
                    if (ruta != null) {
                        callback(Result.success(ruta))
                    } else {
                        callback(Result.failure(Exception("Error al deserializar la ruta")))
                    }
                } else {
                    callback(Result.failure(Exception("La ruta no existe en la base de datos")))
                }
            }
            .addOnFailureListener { exception ->
                callback(Result.failure(exception))
            }
    }
}
