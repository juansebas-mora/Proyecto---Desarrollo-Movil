package com.example.urumbox.accessactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.urumbox.R
import com.example.urumbox.data.model.AccessHistoryItem
import java.text.SimpleDateFormat
import java.util.Locale

class AccessHistoryAdapter : RecyclerView.Adapter<AccessHistoryAdapter.ViewHolder>() {

    private val items = mutableListOf<AccessHistoryItem>()
    private val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun submitList(list: List<AccessHistoryItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_access_history, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvZona: TextView = view.findViewById(R.id.tvZona)
        private val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)

        fun bind(item: AccessHistoryItem) {
            tvZona.text = item.zona
            tvTimestamp.text = item.timestamp?.toDate()?.let { fmt.format(it) } ?: "--"
        }
    }
}
