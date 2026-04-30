package com.example.urumbox

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.urumbox.databinding.ItemObjetoBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ObjetosAdapter(
    private val context: Context,
    private var lista: List<ObjetoPerdido>,
    private val onVerDetalles: (ObjetoPerdido) -> Unit
) : RecyclerView.Adapter<ObjetosAdapter.ObjetoViewHolder>() {

    inner class ObjetoViewHolder(val binding: ItemObjetoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjetoViewHolder {
        val binding = ItemObjetoBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return ObjetoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ObjetoViewHolder, position: Int) {
        val objeto = lista[position]
        val b = holder.binding

        b.tvFechaRelativa.text = getFechaRelativa(objeto.fecha)
        b.ivObjeto.setImageResource(
            objeto.imagenResId ?: R.drawable.ic_objeto
        )
        b.tvNombre.text = objeto.nombre
        b.tvUbicacion.text = objeto.ubicacion

        if (objeto.estado == EstadoObjeto.PERDIDO) {
            b.tvEstado.text = "Perdido"
            b.tvEstado.setBackgroundResource(R.drawable.badge_perdido)
            b.tvEstado.setTextColor(
                ContextCompat.getColor(context, R.color.white)
            )
            b.tvDescripcion.visibility = View.VISIBLE
            b.tvDescripcion.text = objeto.descripcion
        } else {
            b.tvEstado.text = "Encontrado"
            b.tvEstado.setBackgroundResource(R.drawable.badge_encontrado)
            b.tvEstado.setTextColor(
                ContextCompat.getColor(context, R.color.badge_encontrado_text)
            )
            b.tvDescripcion.visibility = View.GONE
        }

        b.btnVerDetalles.setOnClickListener {
            onVerDetalles(objeto)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<ObjetoPerdido>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    private fun getFechaRelativa(fecha: Date): String {
        val diffDias = TimeUnit.MILLISECONDS
            .toDays(Date().time - fecha.time).toInt()
        val hora = SimpleDateFormat("h:mm a", Locale.getDefault()).format(fecha)
        return when (diffDias) {
            0    -> "Hoy $hora"
            1    -> "Ayer $hora"
            else -> "Hace $diffDias días · $hora"
        }
    }
}