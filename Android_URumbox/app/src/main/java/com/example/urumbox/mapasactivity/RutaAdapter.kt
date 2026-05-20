package com.example.urumbox.mapasactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.urumbox.R
import com.example.urumbox.data.model.Ruta

class RutaAdapter(
    private var list: List<Ruta> = emptyList(),
    private val onComoLlegarClick: (Ruta) -> Unit
) : RecyclerView.Adapter<RutaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRutaDestino: TextView = view.findViewById(R.id.tvRutaDestino)
        val tvRutaOrigen: TextView = view.findViewById(R.id.tvRutaOrigen)
        val tvRutaInfoBadge: TextView = view.findViewById(R.id.tvRutaInfoBadge)
        val btnRutaToMap: Button = view.findViewById(R.id.btnRutaToMap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ruta, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvRutaDestino.text = item.destino
        holder.tvRutaOrigen.text = "Desde ${item.origen}"
        
        // Calcular número de pisos recorridos
        val floorsCount = item.coordenadas.map { it.piso }.distinct().size
        val floorsText = if (floorsCount > 1) "$floorsCount pisos" else "1 piso"
        holder.tvRutaInfoBadge.text = floorsText
        
        holder.btnRutaToMap.setOnClickListener {
            onComoLlegarClick(item)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Ruta>) {
        list = newList
        notifyDataSetChanged()
    }
}
