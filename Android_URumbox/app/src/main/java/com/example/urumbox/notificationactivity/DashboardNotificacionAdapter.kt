package com.example.urumbox.notificationactivity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.urumbox.R
import com.example.urumbox.data.model.Notificacion
import com.example.urumbox.databinding.ItemDashboardNotificacionBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DashboardNotificacionAdapter(
    private val onClick: (Notificacion) -> Unit
) : RecyclerView.Adapter<DashboardNotificacionAdapter.ViewHolder>() {

    private var items: List<Notificacion> = emptyList()

    fun submitList(newList: List<Notificacion>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDashboardNotificacionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemDashboardNotificacionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Notificacion) {
            binding.tvTipoNoti.text = item.tipo
            binding.tvZonaNoti.text = "Zona: ${item.zonaAfectada}"
            binding.tvDescNoti.text = item.descripcion
            binding.tvHoraNoti.text = formatearHora(item.timestamp)
            binding.ivIconoNoti.setImageResource(iconoPorTipo(item.tipo))

            binding.root.setOnClickListener {
                onClick(item)
            }
        }

        private fun formatearHora(timestamp: Timestamp?): String =
            timestamp?.let {
                SimpleDateFormat("h:mm a", Locale.forLanguageTag("es"))
                    .apply { timeZone = TimeZone.getTimeZone("America/Bogota") }
                    .format(it.toDate())
            } ?: "Sin hora"

        private fun iconoPorTipo(tipo: String): Int = when (tipo) {
            "Incidente"          -> R.drawable.ic_warning_white
            "Limpieza"           -> R.drawable.ic_restaurar_white
            "Actividad"          -> R.drawable.ic_group_white
            "Acceso Restringido" -> R.drawable.ic_lock_white
            "Ruta Alternativa"   -> R.drawable.ic_help_circle_white
            else                 -> R.drawable.ic_warning_white
        }
    }
}
