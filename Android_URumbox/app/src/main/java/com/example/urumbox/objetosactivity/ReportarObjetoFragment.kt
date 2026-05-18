package com.example.urumbox.objetosactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.urumbox.databinding.FragmentReportarObjetoBinding
import com.example.urumbox.ui.objetosperdidos.ObjetoViewModel
import java.util.Date
import com.example.urumbox.data.model.objetosperdidos.EstadoObjeto
import com.example.urumbox.data.model.objetosperdidos.ObjetoPerdido
import com.example.urumbox.R


class ReportarObjetoFragment : Fragment() {

    private var _binding: FragmentReportarObjetoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ObjetoViewModel

    // Estado del tab seleccionado
    private var estadoSeleccionado: EstadoObjeto = EstadoObjeto.PERDIDO

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportarObjetoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Comparte el ViewModel con ObjetosPerdidosFragment
        viewModel = ViewModelProvider(requireActivity())[ObjetoViewModel::class.java]

        configurarTabs()
        configurarObservadores()
        configurarBotones()
    }

    private fun configurarTabs() {
        binding.btnTabPerdido.setOnClickListener {
            estadoSeleccionado = EstadoObjeto.PERDIDO
            binding.btnTabPerdido.backgroundTintList =
                resources.getColorStateList(R.color.azul_ur, null)
            binding.btnTabPerdido.setTextColor(
                resources.getColor(R.color.blanco, null))
            binding.btnTabEncontrado.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
            binding.btnTabEncontrado.setTextColor(
                resources.getColor(R.color.texto_secundario, null))
        }

        binding.btnTabEncontrado.setOnClickListener {
            estadoSeleccionado = EstadoObjeto.ENCONTRADO
            binding.btnTabEncontrado.backgroundTintList =
                resources.getColorStateList(R.color.azul_ur, null)
            binding.btnTabEncontrado.setTextColor(
                resources.getColor(R.color.blanco, null))
            binding.btnTabPerdido.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
            binding.btnTabPerdido.setTextColor(
                resources.getColor(R.color.texto_secundario, null))
        }
    }

    private fun configurarObservadores() {
        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.btnPublicarReporte.isEnabled = !cargando
            binding.btnPublicarReporte.text =
                if (cargando) "Publicando..." else "Publicar Reporte"
        }

        viewModel.mensaje.observe(viewLifecycleOwner) { mensaje ->
            if (mensaje.isNotEmpty()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                // Si fue exitoso, volver a la lista
                if (mensaje.contains("exitosamente")) {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun configurarBotones() {
        binding.btnVolver.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // ── REGISTRO: envía el objeto a Firestore via ViewModel ───────────
        binding.btnPublicarReporte.setOnClickListener {
            if (validarFormulario()) {
                val categoria = when (binding.chipGroupCategorias.checkedChipId) {
                    R.id.chipTecnologia -> "Tecnología"
                    R.id.chipDocumentos -> "Documentos"
                    R.id.chipPrendas    -> "Prendas"
                    R.id.chipAccesorios -> "Accesorios"
                    R.id.chipOtro       -> "Otro"
                    else                -> "Otro"
                }

                val ubicacion = binding.tvUbicacionSeleccionada.text.toString()
                    .takeIf { it != "Seleccionar en mapa" } ?: ""

                val nuevoObjeto = ObjetoPerdido(
                    nombre             = categoria,
                    descripcion        = binding.etDescripcion.text.toString().trim(),
                    ubicacion          = ubicacion,
                    fecha              = Date(),
                    estado             = estadoSeleccionado,
                    categoria          = categoria
                )

                // Llama al ViewModel → Repository → Firestore
                viewModel.registrarObjeto(nuevoObjeto)
            }
        }
    }

    private fun validarFormulario(): Boolean {
        val descripcion = binding.etDescripcion.text.toString().trim()
        return when {
            descripcion.isEmpty() -> {
                binding.etDescripcion.error = "La descripción es obligatoria"
                Toast.makeText(requireContext(),
                    "Por favor describe el objeto", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}