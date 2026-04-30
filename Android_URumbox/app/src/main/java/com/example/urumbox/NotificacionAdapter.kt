package com.example.urumbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificacionAdapter(
    private val lista: MutableList<Notificacion>,
    private val rolUsuario: String,
    private val onVerDetalles: (Notificacion, Int) -> Unit,
    private val onAceptar: (Notificacion, Int) -> Unit,
    private val onRechazar: (Notificacion, Int) -> Unit,
    private val onRestaurar: (Notificacion, Int) -> Unit,
    private val onEliminarDefinitivo: (Notificacion, Int) -> Unit
) : RecyclerView.Adapter<NotificacionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHora: TextView                        = view.findViewById(R.id.tvHora)
        val tvTipo: TextView                        = view.findViewById(R.id.tvTipo)
        val ivIconoCategoria: ImageView             = view.findViewById(R.id.ivIconoCategoria)
        val tvNombre: TextView                      = view.findViewById(R.id.tvNombre)
        val tvFecha: TextView                       = view.findViewById(R.id.tvFecha)
        val tvArea: TextView                        = view.findViewById(R.id.tvArea)
        val indicadorNoLeido: View                  = view.findViewById(R.id.indicadorNoLeido)
        val btnVerDetalles: android.widget.Button   = view.findViewById(R.id.btnVerDetalles)
        val btnAceptar: ImageButton                 = view.findViewById(R.id.btnAceptar)
        val btnRechazar: ImageButton                = view.findViewById(R.id.btnRechazar)
        val btnRestaurar: android.widget.Button     = view.findViewById(R.id.btnRestaurar)
        val btnEliminarDefinitivo: ImageButton      = view.findViewById(R.id.btnEliminarDefinitivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val n = lista[position]

        holder.tvHora.text   = n.hora
        holder.tvTipo.text   = n.tipo
        holder.tvNombre.text = n.nombreReportante
        holder.tvFecha.text  = n.fecha
        holder.tvArea.text   = n.zonaAfectada
        holder.ivIconoCategoria.setImageResource(n.iconoResId)

        holder.indicadorNoLeido.visibility =
            if (n.leida || n.eliminada) View.INVISIBLE else View.VISIBLE

        if (n.eliminada) {
            holder.btnVerDetalles.visibility        = View.GONE
            holder.btnAceptar.visibility            = View.GONE
            holder.btnRechazar.visibility           = View.GONE
            if (rolUsuario == "Admin" || rolUsuario == "Operador") {
                holder.btnRestaurar.visibility          = View.VISIBLE
                holder.btnEliminarDefinitivo.visibility = View.VISIBLE
            } else {
                holder.btnRestaurar.visibility          = View.GONE
                holder.btnEliminarDefinitivo.visibility = View.GONE
            }
        } else {
            holder.btnRestaurar.visibility          = View.GONE
            holder.btnEliminarDefinitivo.visibility = View.GONE
            holder.btnVerDetalles.visibility        = View.VISIBLE

            when (rolUsuario) {
                "Visitante" -> {
                    holder.btnAceptar.visibility  = View.GONE
                    holder.btnRechazar.visibility = View.GONE
                }
                "Operador", "Admin" -> {
                    holder.btnAceptar.visibility  = View.VISIBLE
                    holder.btnRechazar.visibility = View.VISIBLE
                }
            }
        }

        holder.btnVerDetalles.setOnClickListener        { onVerDetalles(n, position) }
        holder.btnAceptar.setOnClickListener            { onAceptar(n, position) }
        holder.btnRechazar.setOnClickListener           { onRechazar(n, position) }
        holder.btnRestaurar.setOnClickListener          { onRestaurar(n, position) }
        holder.btnEliminarDefinitivo.setOnClickListener { onEliminarDefinitivo(n, position) }
    }

    override fun getItemCount() = lista.size

    fun filtrar(filtro: String, listaCompleta: List<Notificacion>) {
        lista.clear()
        when (filtro) {
            "Sin ver"    -> lista.addAll(listaCompleta.filter { !it.leida && !it.eliminada })
            "Vistos"     -> lista.addAll(listaCompleta.filter { it.leida && !it.eliminada })
            "Archivados" -> {
                if (rolUsuario == "Admin" || rolUsuario == "Operador") {
                    lista.addAll(listaCompleta.filter { it.eliminada })
                } else {
                    lista.addAll(listaCompleta.filter { !it.eliminada })
                }
            }
            else -> lista.addAll(listaCompleta.filter { !it.eliminada })
        }
        notifyDataSetChanged()
    }
}
