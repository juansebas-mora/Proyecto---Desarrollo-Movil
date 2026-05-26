package com.example.urumbox.useractivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.urumbox.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class ComentarioAdapter(
    private var items: MutableList<DocumentSnapshot>,
    private val onItemClick: (DocumentSnapshot) -> Unit
) : RecyclerView.Adapter<ComentarioAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAsunto: TextView = view.findViewById(R.id.tvAsuntoComentario)
        val tvCorreo: TextView = view.findViewById(R.id.tvCorreoComentario)
        val tvContenido: TextView = view.findViewById(R.id.tvContenidoComentario)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaComentario)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoComentario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comentario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = items[position]
        holder.tvAsunto.text = doc.getString("asunto") ?: ""
        holder.tvCorreo.text = doc.getString("correo") ?: ""
        holder.tvContenido.text = doc.getString("comentario") ?: ""
        holder.tvEstado.text = doc.getString("estado") ?: "pendiente"

        val timestamp = doc.getTimestamp("fecha")
        holder.tvFecha.text = if (timestamp != null) {
            dateFormat.format(timestamp.toDate())
        } else ""

        holder.itemView.setOnClickListener { onItemClick(doc) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<DocumentSnapshot>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
