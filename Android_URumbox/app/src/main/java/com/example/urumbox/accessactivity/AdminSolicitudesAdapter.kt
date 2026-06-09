package com.example.urumbox.accessactivity

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.urumbox.R
import com.example.urumbox.data.model.AccessRequest
import com.google.android.material.button.MaterialButton

class AdminSolicitudesAdapter(
    private val items: MutableList<AccessRequest>,
    private val onAceptar: (AccessRequest) -> Unit,
    private val onDenegar: (AccessRequest) -> Unit
) : RecyclerView.Adapter<AdminSolicitudesAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreVisitante)
        val tvCorreo: TextView = view.findViewById(R.id.tvCorreoVisitante)
        val tvDocumento: TextView = view.findViewById(R.id.tvDocumentoVisitante)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaVisitante)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoSolicitud)
        val layoutBotones: View = view.findViewById(R.id.layoutBotonesSolicitud)
        val btnAceptar: MaterialButton = view.findViewById(R.id.btnAceptarSolicitud)
        val btnDenegar: MaterialButton = view.findViewById(R.id.btnDenegarSolicitud)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitud_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val ctx = holder.itemView.context

        holder.tvNombre.text = "${item.nombres} ${item.apellidos}"
        holder.tvCorreo.text = item.correo
        holder.tvDocumento.text = "Doc: ${item.documento}"
        holder.tvFecha.text = "Fecha: ${item.fecha}"

        val (estadoLabel, badgeColor, textColor) = when (item.estado) {
            "aceptada" -> Triple(
                "Aceptada",
                ContextCompat.getColor(ctx, R.color.badge_encontrado_bg),
                ContextCompat.getColor(ctx, R.color.blanco)
            )
            "denegada" -> Triple(
                "Denegada",
                0xFFFFE0E0.toInt(),
                ContextCompat.getColor(ctx, R.color.text_alto)
            )
            else -> Triple(
                "Pendiente",
                0xFFF0F0F0.toInt(),
                ContextCompat.getColor(ctx, R.color.texto_secundario)
            )
        }

        holder.tvEstado.text = estadoLabel
        holder.tvEstado.setTextColor(textColor)

        val badge = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f * ctx.resources.displayMetrics.density
            setColor(badgeColor)
        }
        holder.tvEstado.background = badge

        if (item.estado == "pendiente") {
            holder.layoutBotones.visibility = View.VISIBLE
            holder.btnAceptar.setOnClickListener { onAceptar(item) }
            holder.btnDenegar.setOnClickListener { onDenegar(item) }
        } else {
            holder.layoutBotones.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<AccessRequest>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
