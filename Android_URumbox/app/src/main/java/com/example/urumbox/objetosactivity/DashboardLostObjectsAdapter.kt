package com.example.urumbox.objetosactivity

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.urumbox.R
import com.example.urumbox.data.model.objetosperdidos.EstadoObjeto
import com.example.urumbox.data.model.objetosperdidos.ObjetoPerdido
import com.example.urumbox.databinding.ItemDashboardObjetoBinding
import com.example.urumbox.mapasactivity.MapaActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DashboardLostObjectsAdapter(
    private val onDetailsClick: (ObjetoPerdido) -> Unit
) : RecyclerView.Adapter<DashboardLostObjectsAdapter.ViewHolder>() {

    private var items: List<ObjetoPerdido> = emptyList()

    fun submitList(newList: List<ObjetoPerdido>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDashboardObjetoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemDashboardObjetoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ObjetoPerdido) {
            val context = itemView.context
            
            binding.tvFechaRelativa.text = getFechaRelativa(item.fecha)
            binding.ivObjeto.setImageResource(item.imagenResId ?: R.drawable.ic_objeto)
            binding.tvNombre.text = item.nombre
            binding.tvUbicacion.text = item.ubicacion
            binding.tvDescripcion.text = item.descripcion

            if (item.estado == EstadoObjeto.PERDIDO) {
                binding.tvEstado.text = "Perdido"
                binding.tvEstado.setBackgroundResource(R.drawable.badge_perdido)
                binding.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                binding.tvEstado.text = "Encontrado"
                binding.tvEstado.setBackgroundResource(R.drawable.badge_encontrado)
                binding.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.blanco))
            }

            // Configurar "Cómo llegar" dinámicamente según la ubicación
            binding.btnObjetoRoute.setOnClickListener {
                val ubicacionLower = item.ubicacion.lowercase(Locale.getDefault())
                val idRuta = when {
                    ubicacionLower.contains("casur") -> "ruta_casur"
                    ubicacionLower.contains("rector") -> "ruta_rectoria"
                    ubicacionLower.contains("auditorio") -> "ruta_auditorio"
                    else -> "ruta_casur" // Fallback por defecto
                }
                val intent = Intent(context, MapaActivity::class.java).apply {
                    putExtra("id_ruta", idRuta)
                }
                context.startActivity(intent)
            }

            binding.btnObjetoDetails.setOnClickListener {
                onDetailsClick(item)
            }
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
}
