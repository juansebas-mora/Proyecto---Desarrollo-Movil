package com.example.urumbox

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.urumbox.databinding.FragmentReportarObjetoBinding
import java.util.Date

class ReportarObjetoFragment : Fragment() {

    private var _binding: FragmentReportarObjetoBinding? = null
    private val binding get() = _binding!!

    private var estadoSeleccionado: EstadoObjeto = EstadoObjeto.PERDIDO
    private var fotoUri: Uri? = null

    // Lanzador para galería / cámara
    private val seleccionarFotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fotoUri = result.data?.data
                fotoUri?.let {
                    binding.ivFotoPreview.setImageURI(it)
                    binding.ivFotoPreview.visibility = View.VISIBLE
                    binding.tvFotoEstado.text = "Foto seleccionada ✓"
                }
            }
        }

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
        configurarTabs()
        configurarFoto()
        configurarMapa()
        configurarPublicar()
        //configurarNavbar()

        binding.btnVolver.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────

    private fun configurarTabs() {
        actualizarEstiloTabs(EstadoObjeto.PERDIDO)

        binding.btnTabPerdido.setOnClickListener {
            estadoSeleccionado = EstadoObjeto.PERDIDO
            actualizarEstiloTabs(EstadoObjeto.PERDIDO)
        }
        binding.btnTabEncontrado.setOnClickListener {
            estadoSeleccionado = EstadoObjeto.ENCONTRADO
            actualizarEstiloTabs(EstadoObjeto.ENCONTRADO)
        }
    }

    private fun actualizarEstiloTabs(activo: EstadoObjeto) {
        val colorActivo   = resources.getColor(R.color.azul_ur, null)
        val colorInactivo = resources.getColor(android.R.color.transparent, null)
        val textoActivo   = resources.getColor(R.color.blanco, null)
        val textoInactivo = resources.getColor(R.color.texto_secundario, null)

        if (activo == EstadoObjeto.PERDIDO) {
            binding.btnTabPerdido.backgroundTintList =
                android.content.res.ColorStateList.valueOf(colorActivo)
            binding.btnTabPerdido.setTextColor(textoActivo)
            binding.btnTabEncontrado.backgroundTintList =
                android.content.res.ColorStateList.valueOf(colorInactivo)
            binding.btnTabEncontrado.setTextColor(textoInactivo)

        } else {
            binding.btnTabEncontrado.backgroundTintList =
                android.content.res.ColorStateList.valueOf(colorActivo)
            binding.btnTabEncontrado.setTextColor(textoActivo)
            binding.btnTabPerdido.backgroundTintList =
                android.content.res.ColorStateList.valueOf(colorInactivo)
            binding.btnTabPerdido.setTextColor(textoInactivo)
        }
    }

    // ── Foto ──────────────────────────────────────────────────────────────────

    private fun configurarFoto() {
        binding.btnSubirFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            seleccionarFotoLauncher.launch(intent)
        }
    }

    // ── Mapa (placeholder) ────────────────────────────────────────────────────

    private fun configurarMapa() {
        binding.btnSeleccionarMapa.setOnClickListener {
            // TODO: cuando el módulo de mapas esté listo, navegar a MapaFragment
            // Por ahora simulamos una selección
            binding.tvUbicacionSeleccionada.text = "Biblioteca Central"
            binding.tvUbicacionSeleccionada.setTextColor(
                resources.getColor(R.color.azul_ur, null)
            )
        }
    }

    // ── Publicar ──────────────────────────────────────────────────────────────

    private fun configurarPublicar() {
        binding.btnPublicarReporte.setOnClickListener {
            if (validarFormulario()) {
                val reporte = construirReporte()
                enviarReporte(reporte)
            }
        }
    }

    private fun validarFormulario(): Boolean {
        val descripcion = binding.etDescripcion.text.toString().trim()

        if (descripcion.isEmpty()) {
            binding.etDescripcion.error = "La descripción es obligatoria"
            binding.etDescripcion.requestFocus()
            return false
        }
        if (binding.tvUbicacionSeleccionada.text == "Seleccionar en mapa") {
            mostrarError("Selecciona una ubicación en el mapa")
            return false
        }
        return true
    }

    private fun obtenerCategoriaSeleccionada(): String {
        val chipId = binding.chipGroupCategorias.checkedChipId
        return when (chipId) {
            R.id.chipTecnologia  -> "Tecnología"
            R.id.chipDocumentos  -> "Documentos"
            R.id.chipPrendas     -> "Prendas"
            R.id.chipAccesorios  -> "Accesorios"
            R.id.chipOtro        -> "Otro"
            else                 -> "Otro"
        }
    }

    private fun construirReporte(): ObjetoPerdido {
        return ObjetoPerdido(
            id          = 0, // el backend asignará el id real
            nombre      = obtenerCategoriaSeleccionada(),
            ubicacion   = binding.tvUbicacionSeleccionada.text.toString(),
            descripcion = binding.etDescripcion.text.toString().trim(),
            fecha       = Date(),
            estado      = estadoSeleccionado,
            categoria   = obtenerCategoriaSeleccionada(),
            fotoUri     = fotoUri?.toString()
        )
    }

    private fun enviarReporte(reporte: ObjetoPerdido) {
        // TODO: reemplazar con llamada real al backend/ViewModel
        // Por ahora simula éxito y muestra el modal
        mostrarDialogoExito()
    }

    // ── Dialog de éxito ───────────────────────────────────────────────────────

    private fun mostrarDialogoExito() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_reporte_publicado)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)

        dialog.findViewById<Button>(R.id.btnCerrarDialog).setOnClickListener {
            dialog.dismiss()
            parentFragmentManager.popBackStack() // regresa al listado
        }

        dialog.show()
    }

    private fun mostrarError(mensaje: String) {
        android.widget.Toast.makeText(requireContext(), mensaje, android.widget.Toast.LENGTH_SHORT).show()
    }

    // ── Navbar ────────────────────────────────────────────────────────────────

    /*private fun configurarNavbar() {
        binding.navbar.setOnButtonsClickListener(
            onHome      = { /* TODO */ },
            onBox       = { parentFragmentManager.popBackStack() },
            onAccess    = { /* TODO */ },
            onEmergency = { /* TODO */ }
        )
    */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}