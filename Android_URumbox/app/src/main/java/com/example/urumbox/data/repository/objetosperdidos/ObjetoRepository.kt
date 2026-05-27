package com.example.urumbox.data.repository.objetosperdidos

import android.net.Uri
import com.example.urumbox.data.model.objetosperdidos.ObjetoPerdido
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class ObjetoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val coleccion = db.collection("objetos_perdidos")

    private val storage = FirebaseStorage.getInstance()

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

    fun subirImagen(
        uri: Uri,
        onExito: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {

        val nombreArchivo = "objetos/${System.currentTimeMillis()}.jpg"

        val referencia = storage.reference.child(nombreArchivo)

        referencia.putFile(uri)
            .addOnSuccessListener {

                referencia.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        onExito(downloadUri.toString())
                    }

            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
    fun obtenerObjetos(
        onExito: (List<ObjetoPerdido>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        coleccion
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.documents.map { doc ->
                    ObjetoPerdido.fromDocument(doc)
                }
                onExito(lista)
            }
            .addOnFailureListener { e -> onError(e) }
    }

    fun obtenerDatosUsuarioActual(
        onExito: (nombre: String, telefono: String, correo: String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        val correoAuth = auth.currentUser?.email ?: ""

        if (uid == null) {
            onExito("", "", correoAuth)
            return
        }

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val nombre   = doc.getString("nombreCompleto") ?: ""
                val telefono = doc.getString("telefono") ?: ""
                val correo   = doc.getString("correo") ?: correoAuth
                onExito(nombre, telefono, correo)
            }
            .addOnFailureListener { e -> onError(e) }
    }
}