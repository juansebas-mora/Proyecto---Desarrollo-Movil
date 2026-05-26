package com.example.urumbox.useractivity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urumbox.R
import com.example.urumbox.databinding.ActivityVerComentariosBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class VerComentariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerComentariosBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ComentarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityVerComentariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.topBar.setOnBackClickListener { finish() }

        adapter = ComentarioAdapter(mutableListOf()) { doc ->
            mostrarOpcionesComentario(doc.id, doc.getString("estado") ?: "pendiente")
        }
        binding.recyclerComentarios.layoutManager = LinearLayoutManager(this)
        binding.recyclerComentarios.adapter = adapter

        cargarComentarios()
    }

    private fun cargarComentarios() {
        db.collection("comentarios")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                adapter.updateItems(snapshot.documents)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar comentarios", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarOpcionesComentario(docId: String, estadoActual: String) {
        val opciones = arrayOf("Marcar como revisado", "Eliminar comentario")
        AlertDialog.Builder(this)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> marcarComoRevisado(docId)
                    1 -> eliminarComentario(docId)
                }
            }
            .show()
    }

    private fun marcarComoRevisado(docId: String) {
        db.collection("comentarios").document(docId)
            .update("estado", "revisado")
            .addOnSuccessListener {
                Toast.makeText(this, "Comentario marcado como revisado", Toast.LENGTH_SHORT).show()
                cargarComentarios()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarComentario(docId: String) {
        db.collection("comentarios").document(docId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Comentario eliminado", Toast.LENGTH_SHORT).show()
                cargarComentarios()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }
}
