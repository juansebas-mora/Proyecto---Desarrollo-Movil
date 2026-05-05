package com.example.urumbox.accessactivity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.urumbox.data.model.AccessRequest
import com.example.urumbox.databinding.ItemAccessRequestBinding

class AccessRequestAdapter(
    private var items: List<AccessRequest> = emptyList(),
    private val onDetailClick: (AccessRequest) -> Unit
) : RecyclerView.Adapter<AccessRequestAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAccessRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAccessRequestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = items[position]
        val primerNombre = request.nombres.trim().split("\\s+".toRegex()).firstOrNull() ?: ""
        val primerApellido = request.apellidos.trim().split("\\s+".toRegex()).firstOrNull() ?: ""
        holder.binding.tvNombre.text = "$primerNombre $primerApellido"
        holder.binding.tvFecha.text = request.fecha
        holder.binding.btnDetails.setOnClickListener { onDetailClick(request) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<AccessRequest>) {
        items = newItems
        notifyDataSetChanged()
    }
}
