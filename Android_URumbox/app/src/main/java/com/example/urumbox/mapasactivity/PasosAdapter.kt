package com.example.urumbox.mapasactivity

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.urumbox.R
import com.example.urumbox.data.model.PasoNav
import com.example.urumbox.databinding.ItemPasoBinding

class PasosAdapter(
    private var pasos: List<PasoNav> = emptyList(),
    private var activeIndex: Int = 0,
    private val onStepClick: (Int) -> Unit
) : RecyclerView.Adapter<PasosAdapter.PasoViewHolder>() {

    fun updateData(newPasos: List<PasoNav>, newActiveIndex: Int) {
        this.pasos = newPasos
        this.activeIndex = newActiveIndex
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasoViewHolder {
        val binding = ItemPasoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PasoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PasoViewHolder, position: Int) {
        holder.bind(pasos[position], position)
    }

    override fun getItemCount(): Int = pasos.size

    inner class PasoViewHolder(private val binding: ItemPasoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(paso: PasoNav, position: Int) {
            binding.tvStepTitle.text = paso.titulo
            
            if (paso.descripcion.isNullOrBlank()) {
                binding.tvStepDesc.visibility = View.GONE
            } else {
                binding.tvStepDesc.visibility = View.VISIBLE
                binding.tvStepDesc.text = paso.descripcion
            }

            // Set icon resource
            binding.ivStepIcon.setImageResource(getIconResource(paso.icono))

            // Style and text for button based on active step status
            when {
                position == activeIndex -> {
                    val isLastStep = position == pasos.size - 1
                    binding.btnStepAction.text = if (isLastStep) "Llegué" else "Estás aquí"
                    binding.btnStepAction.setBackgroundResource(R.drawable.bg_button_light_blue_rounded_10)
                    binding.btnStepAction.setTextColor(Color.parseColor("#8E8E93"))
                    binding.btnStepAction.isEnabled = false
                }
                position < activeIndex -> {
                    binding.btnStepAction.text = "Volver aquí"
                    binding.btnStepAction.setBackgroundResource(R.drawable.bg_button_rounded_10)
                    binding.btnStepAction.setTextColor(Color.WHITE)
                    binding.btnStepAction.isEnabled = true
                }
                else -> {
                    binding.btnStepAction.text = "Saltar aquí"
                    binding.btnStepAction.setBackgroundResource(R.drawable.bg_button_rounded_10)
                    binding.btnStepAction.setTextColor(Color.WHITE)
                    binding.btnStepAction.isEnabled = true
                }
            }

            binding.btnStepAction.setOnClickListener {
                onStepClick(position)
            }
        }

        private fun getIconResource(iconoName: String): Int {
            return when (iconoName) {
                "ic_stairs" -> R.drawable.ic_stairs
                "ic_arrive" -> R.drawable.ic_location_check_destination
                else -> R.drawable.ic_location_current_original
            }
        }
    }
}
