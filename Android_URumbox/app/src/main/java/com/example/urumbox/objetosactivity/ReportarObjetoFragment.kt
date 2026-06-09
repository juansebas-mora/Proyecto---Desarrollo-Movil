package com.example.urumbox.ui.objetosperdidos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.urumbox.R
import com.example.urumbox.data.model.objetosperdidos.EstadoObjeto
import com.example.urumbox.data.model.objetosperdidos.ObjetoPerdido
import com.example.urumbox.databinding.FragmentReportarObjetoBinding
import java.util.Date
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import androidx.appcompat.app.AlertDialog

class ReportarObjetoFragment : Fragment() {

    private var _binding: FragmentReportarObjetoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ObjetoViewModel

    private var estadoSeleccionado: EstadoObjeto = EstadoObjeto.PERDIDO

    // Datos del usuario que se llenan automáticamente
    private var nombreUsuario: String = ""
    private var telefonoUsuario: String = ""
    private var correoUsuario: String = ""

    private var latitudObjeto: Double = 0.0
    private var longitudObjeto: Double = 0.0

    private var fotoUri: Uri? = null
    private var cameraUri: Uri? = null

    private var categoriaPersonalizada: String = ""

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

        viewModel = ViewModelProvider(requireActivity())[ObjetoViewModel::class.java]

        configurarObservadores()
        configurarTabs()
        configurarBotones()

        // ── Carga automática de datos del usuario al abrir el formulario ──
        viewModel.cargarDatosUsuario()
        configurarCategoriaOtro()
    }

    private fun configurarObservadores() {
        // Rellena los campos del reportante con datos del usuario autenticado
        viewModel.datosUsuario.observe(viewLifecycleOwner) { (nombre, telefono, correo) ->
            nombreUsuario = nombre
            telefonoUsuario = telefono
            correoUsuario = correo
        }

        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.btnPublicarReporte.isEnabled = !cargando
            binding.btnPublicarReporte.text =
                if (cargando) "Publicando..." else "Publicar Reporte"
        }

        viewModel.mensaje.observe(viewLifecycleOwner) { mensaje ->
            if (mensaje.isNotEmpty()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                if (mensaje.contains("exitosamente")) {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun configurarTabs() {
        binding.btnTabPerdido.setOnClickListener {
            estadoSeleccionado = EstadoObjeto.PERDIDO
            binding.btnTabPerdido.backgroundTintList =
                resources.getColorStateList(R.color.azul_ur, null)
            binding.btnTabPerdido.setTextColor(
                resources.getColor(R.color.blanco, null)
            )
            binding.btnTabEncontrado.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
            binding.btnTabEncontrado.setTextColor(
                resources.getColor(R.color.texto_secundario, null)
            )
        }

        binding.btnTabEncontrado.setOnClickListener {
            estadoSeleccionado = EstadoObjeto.ENCONTRADO
            binding.btnTabEncontrado.backgroundTintList =
                resources.getColorStateList(R.color.azul_ur, null)
            binding.btnTabEncontrado.setTextColor(
                resources.getColor(R.color.blanco, null)
            )
            binding.btnTabPerdido.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
            binding.btnTabPerdido.setTextColor(
                resources.getColor(R.color.texto_secundario, null)
            )
        }
    }

    private fun configurarBotones() {
//        binding.btnVolver.setOnClickListener {
//            parentFragmentManager.popBackStack()
//        }

        // ── NUEVO: abre el mapa al tocar la sección de ubicación ─────────────
        binding.btnSeleccionarMapa.setOnClickListener {
            val sheet = MapaBottomSheet()
            sheet.onUbicacionSeleccionada = { lat, lng, direccion ->
                latitudObjeto = lat
                longitudObjeto = lng
                binding.tvUbicacionSeleccionada.text = direccion
                binding.tvUbicacionSeleccionada.setTextColor(
                    resources.getColor(R.color.texto_principal, null)
                )
            }
            sheet.show(parentFragmentManager, MapaBottomSheet.TAG)
        }

        binding.btnPublicarReporte.setOnClickListener {

            if (validarFormulario()) {

                val categoria = when (binding.chipGroupCategorias.checkedChipId) {
                    R.id.chipTecnologia -> "Tecnología"
                    R.id.chipDocumentos -> "Documentos"
                    R.id.chipPrendas -> "Prendas"
                    R.id.chipAccesorios -> "Accesorios"
                    R.id.chipOtro -> categoriaPersonalizada.ifEmpty { "Otro" }
                    else -> "Otro"
                }

                val ubicacion = binding.tvUbicacionSeleccionada.text.toString()
                    .takeIf { it != "Seleccionar en mapa" } ?: ""

                val nuevoObjeto = ObjetoPerdido(
                    nombre = categoria,
                    descripcion = binding.etDescripcion.text.toString().trim(),
                    ubicacion = ubicacion,
                    latitud = latitudObjeto,
                    longitud = longitudObjeto,
                    fecha = Date(),
                    estado = estadoSeleccionado,
                    categoria = categoria,

                    // GUARDAMOS LA URI LOCAL
                    fotoUri = fotoUri?.toString(),

                    nombreReportante = nombreUsuario,
                    telefonoReportante = telefonoUsuario,
                    correoReportante = correoUsuario
                )

                viewModel.registrarObjeto(nuevoObjeto)
            }
        }
        binding.btnSubirFoto.setOnClickListener {
            mostrarDialogoFoto()
        }

        configurarCategoriaOtro()
    }

    private fun validarFormulario(): Boolean {
        val descripcion = binding.etDescripcion.text.toString().trim()
        return when {
            descripcion.isEmpty() -> {
                binding.etDescripcion.error = "La descripción es obligatoria"
                Toast.makeText(
                    requireContext(),
                    "Por favor describe el objeto", Toast.LENGTH_SHORT
                ).show()
                false
            }

            else -> true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val galeriaLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->

        uri?.let {
            fotoUri = it
            mostrarPreview(it)
        }
    }

    private val camaraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->

        if (success) {

            cameraUri?.let {
                fotoUri = it
                mostrarPreview(it)
            }
        }
    }
    private val permisoCamaraLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->

        if (granted) abrirCamara()
    }
    private fun abrirCamara() {

        val archivoFoto = File(
            requireContext().cacheDir,
            "objeto_${System.currentTimeMillis()}.jpg"
        )

        cameraUri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.urumbox.fileprovider",
            archivoFoto
        )

        camaraLauncher.launch(cameraUri!!)
    }
    private fun mostrarPreview(uri: Uri) {

        binding.ivFotoPreview.setImageURI(uri)
        binding.ivFotoPreview.visibility = View.VISIBLE

        binding.tvFotoEstado.text = "Foto seleccionada"
    }
    private fun mostrarDialogoFoto() {

        AlertDialog.Builder(requireContext())
            .setTitle("Agregar foto")
            .setItems(arrayOf("Tomar foto", "Elegir de galería")) { _, which ->

                when (which) {

                    0 -> verificarPermisoCamara()

                    1 -> galeriaLauncher.launch("image/*")
                }
            }
            .show()
    }
    private fun verificarPermisoCamara() {

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            abrirCamara()

        } else {

            permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    private fun configurarCategoriaOtro() {

        binding.chipGroupCategorias.setOnCheckedStateChangeListener { _, checkedIds ->

            if (checkedIds.contains(R.id.chipOtro)) {
                mostrarDialogoCategoriaOtro()
            }
        }
    }
    private fun mostrarDialogoCategoriaOtro() {

        val input = android.widget.EditText(requireContext()).apply {
            hint = "Ej: Llaves, Botella, Carnet..."
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Otra categoría")
            .setMessage("Ingresa la categoría del objeto")
            .setView(input)

            .setPositiveButton("Guardar") { _, _ ->

                val texto = input.text.toString().trim()

                if (texto.isNotEmpty()) {

                    categoriaPersonalizada = texto

                    binding.chipOtro.text = texto
                } else {

                    Toast.makeText(
                        requireContext(),
                        "Debes ingresar una categoría",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.chipTecnologia.isChecked = true
                }
            }

            .setNegativeButton("Cancelar") { _, _ ->

                binding.chipTecnologia.isChecked = true
            }

            .setCancelable(false)
            .show()
    }

}