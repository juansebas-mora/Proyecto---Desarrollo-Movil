package com.example.urumbox.useractivity

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.urumbox.R

data class UsuarioItem(
    val uid: String,
    val nombreCompleto: String,
    val correo: String,
    val rol: String,
    val estado: String,
    val fotoPerfil: String?
)

class UsuarioAdapter(
    private val items: MutableList<UsuarioItem>,
    private val onOpciones: (UsuarioItem) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgFoto: ImageView = view.findViewById(R.id.imgFotoUsuario)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreUsuario)
        val tvCorreo: TextView = view.findViewById(R.id.tvCorreoUsuario)
        val tvRol: TextView = view.findViewById(R.id.tvRolUsuario)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoUsuario)
        val btnOpciones: ImageButton = view.findViewById(R.id.btnOpcionesUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = items[position]

        holder.tvNombre.text = usuario.nombreCompleto
        holder.tvCorreo.text = usuario.correo
        holder.tvRol.text = if (usuario.rol.isEmpty()) "Sin rol asignado" else usuario.rol

        if (usuario.estado == "activo") {
            holder.tvEstado.text = "Activo"
            holder.tvEstado.setTextColor(Color.parseColor("#00C48C"))
        } else {
            holder.tvEstado.text = "Inactivo"
            holder.tvEstado.setTextColor(holder.itemView.context.getColor(R.color.rojo_ur))
        }

        if (!usuario.fotoPerfil.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(usuario.fotoPerfil)
                .circleCrop()
                .into(holder.imgFoto)
        } else {
            holder.imgFoto.setImageResource(R.drawable.ic_persona)
        }

        holder.btnOpciones.setOnClickListener { onOpciones(usuario) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<UsuarioItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
