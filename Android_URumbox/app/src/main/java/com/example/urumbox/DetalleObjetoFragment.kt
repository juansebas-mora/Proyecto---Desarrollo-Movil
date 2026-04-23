package com.example.urumbox

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.urumbox.databinding.FragmentDetalleObjetoBinding
import java.text.SimpleDateFormat
import java.util.Locale

class DetalleObjetoFragment : Fragment() {

    private var _binding: FragmentDetalleObjetoBinding? = null
    private val binding get() = _binding!!

    private lateinit var objeto: ObjetoPerdido

    companion object {
        private const val ARG_OBJETO_ID = "objeto_id"

        // Por ahora recibimos el objeto completo via argumentos serializados
        // Cuando tengas ViewModel/Repository, solo pasarás el ID
        fun newInstance(objeto: ObjetoPerdido): DetalleObjetoFragment {
            val fragment = DetalleObjetoFragment()
            val bundle = Bundle().apply {
                putInt(ARG_OBJETO_ID, objeto.id)
                putString("nombre",        objeto.nombre)
                putString("ubicacion",     objeto.ubicacion)
                putString("descripcion",   objeto.descripcion)
                putString("estado",        objeto.estado.name)
                putString("categoria",     objeto.categoria)
                putString("fotoUri",       objeto.fotoUri)
                putLong("fecha",           objeto.fecha.time)
                putString("nombreReportante",   objeto.nombreReportante)
                putString("telefonoReportante", objeto.telefonoReportante)
                putString("correoReportante",   objeto.correoReportante)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleObjetoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarDatos()
        configurarUI()
        //configurarNavbar()
    }

    private fun cargarDatos() {
        val args = requireArguments()
        objeto = ObjetoPerdido(
            id                 = args.getInt(ARG_OBJETO_ID),
            nombre             = args.getString("nombre", ""),
            ubicacion          = args.getString("ubicacion", ""),
            descripcion        = args.getString("descripcion", ""),
            fecha              = java.util.Date(args.getLong("fecha")),
            estado             = EstadoObjeto.valueOf(args.getString("estado", "PERDIDO")),
            categoria          = args.getString("categoria", ""),
            fotoUri            = args.getString("fotoUri"),
            nombreReportante   = args.getString("nombreReportante", ""),
            telefonoReportante = args.getString("telefonoReportante", ""),
            correoReportante   = args.getString("correoReportante", "")
        )
    }

    private fun configurarUI() {
        // Foto
        if (!objeto.fotoUri.isNullOrEmpty()) {
            binding.ivFotoDetalle.setImageURI(android.net.Uri.parse(objeto.fotoUri))
        }

        // Badge estado
        if (objeto.estado == EstadoObjeto.PERDIDO) {
            binding.tvBadgeEstado.text = "Perdido"
            binding.tvBadgeEstado.setBackgroundResource(R.drawable.badge_perdido)
        } else {
            binding.tvBadgeEstado.text = "Encontrado"
            binding.tvBadgeEstado.setBackgroundResource(R.drawable.badge_encontrado)
        }

        // Nombre y fecha
        binding.tvNombreObjeto.text = objeto.nombre
        binding.tvFechaReporte.text = formatearFecha(objeto)

        // Chips info
        binding.tvCategoriaDetalle.text  = objeto.categoria.ifEmpty { "Sin categoría" }
        binding.tvUbicacionDetalle.text  = objeto.ubicacion

        // Descripción
        binding.tvDescripcionDetalle.text = objeto.descripcion

        // Reportante
        binding.tvNombreReportante.text = objeto.nombreReportante
        binding.tvRolReportante.text = if (objeto.estado == EstadoObjeto.PERDIDO)
            "Propietario Reportante"
        else
            "Quien lo encontró\nReportante"

        // Botón volver
        binding.btnVolver.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Botón contactar
        binding.btnContactar.setOnClickListener {
            mostrarDialogoContactar()
        }
    }

    private fun formatearFecha(objeto: ObjetoPerdido): String {
        val ahora   = System.currentTimeMillis()
        val diff    = ahora - objeto.fecha.time
        val horas   = diff / (1000 * 60 * 60)
        val sdf     = SimpleDateFormat("h:mm a", Locale("es", "CO"))
        val hora    = sdf.format(objeto.fecha)

        return when {
            horas < 24  -> "Reportado hoy a las $hora"
            horas < 48  -> "Reportado ayer a las $hora"
            else        -> {
                val sdfFull = SimpleDateFormat("d MMM 'a las' h:mm a", Locale("es", "CO"))
                "Reportado el ${sdfFull.format(objeto.fecha)}"
            }
        }
    }

    private fun mostrarDialogoContactar() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_contactar)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)

        dialog.findViewById<TextView>(R.id.tvDialogNombre).text    = objeto.nombreReportante
        dialog.findViewById<TextView>(R.id.tvDialogTelefono).text  = objeto.telefonoReportante
        dialog.findViewById<TextView>(R.id.tvDialogCorreo).text    = objeto.correoReportante

        dialog.findViewById<Button>(R.id.btnCerrarContactar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /*private fun configurarNavbar() {
        binding.navbar.setOnButtonsClickListener(
            onHome      = { /* TODO */ },
            onBox       = { parentFragmentManager.popBackStack() },
            onAccess    = { /* TODO */ },
            onEmergency = { /* TODO */ }
        )
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}