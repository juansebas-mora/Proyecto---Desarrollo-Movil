package com.example.urumbox.data.repository.objetosperdidos

import com.example.urumbox.data.model.objetosperdidos.ObjetoPerdido
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ObjetoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("objetos_perdidos")

    // ── REGISTRO: guarda un objeto en Firestore ────────────────────────────
    fun registrarObjeto(
        objeto: ObjetoPerdido,
        onExito: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        coleccion
            .add(objeto.toMap())
            .addOnSuccessListener { onExito() }
            .addOnFailureListener { e -> onError(e) }
    }

    // ── CONSULTA: obtiene todos los objetos ordenados por fecha ───────────
    fun obtenerObjetos(
        onExito: (List<ObjetoPerdido>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        coleccion
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.documents.map { doc ->
                    ObjetoPerdido.Companion.fromDocument(doc)
                }
                onExito(lista)
            }
            .addOnFailureListener { e -> onError(e) }
    }
}