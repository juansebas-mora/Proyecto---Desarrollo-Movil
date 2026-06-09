package com.example.urumbox.mapasactivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.urumbox.R
import com.example.urumbox.databinding.ActivityMapaBinding
import com.example.urumbox.data.repository.NavegacionManager

class MapaActivity : AppCompatActivity() {

    private val viewModel: MapaViewModel by viewModels()
    private lateinit var binding: ActivityMapaBinding
    private var isShowingSteps = false
    private lateinit var pasosAdapter: PasosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val idRuta = intent.getStringExtra("id_ruta") ?: "ruta_claustro_test"

        // Cargar la ruta desde Firebase Firestore
        viewModel.cargarRuta(idRuta)

        // Observar la ruta cargada del ViewModel para iniciar el gestor de navegación
        viewModel.ruta.observe(this) { ruta ->
            if (ruta != null) {
                NavegacionManager.iniciarNavegacion(ruta)
            }
        }

        // Observar errores al cargar la ruta
        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        }

        // Observar el gestor de navegación singleton
        NavegacionManager.rutaActiva.observe(this) { ruta ->
            if (ruta != null) {
                actualizarInterfazNavegacion()
            }
        }

        NavegacionManager.pasoActualIndex.observe(this) {
            actualizarInterfazNavegacion()
        }

        // Configurar botones de navegación entre pasos
        binding.btnPrevStep.setOnClickListener {
            NavegacionManager.retrocederPaso()
        }

        binding.btnNextStep.setOnClickListener {
            val index = NavegacionManager.pasoActualIndex.value ?: 0
            val totalPasos = NavegacionManager.rutaActiva.value?.pasos?.size ?: 0
            if (index >= totalPasos - 1) {
                Toast.makeText(this, "¡Has llegado a tu destino!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                NavegacionManager.avanzarPaso()
            }
        }

        // Configurar el botón flotante unificado de Mapa/Pasos
        binding.btnMapToggle.setOnClickListener {
            toggleMapStepsView(!isShowingSteps)
        }

        // Configurar RecyclerView para el listado de pasos dinámicos
        binding.rvPasos.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        pasosAdapter = PasosAdapter(emptyList(), 0) { position ->
            NavegacionManager.establecerPasoIndex(position)
            toggleMapStepsView(showSteps = false)
        }
        binding.rvPasos.adapter = pasosAdapter
    }

    private fun actualizarInterfazNavegacion() {
        val ruta = NavegacionManager.rutaActiva.value ?: return
        val index = NavegacionManager.pasoActualIndex.value ?: 0
        val paso = ruta.pasos.getOrNull(index) ?: return

        // Actualizar la vista del mapa con los datos y el paso actual
        binding.interactiveMapView.setRouteData(ruta.coordenadas, paso)

        // Actualizar los textos de información en la parte inferior
        binding.tvDestinationName.text = ruta.destino
        binding.tvRouteStatus.text = "Piso ${paso.piso} | Paso ${paso.num_paso} de ${ruta.pasos.size} (${paso.pasos_requeridos} pasos)"
        
        binding.tvStepTitle.text = paso.titulo
        if (paso.descripcion.isNullOrBlank()) {
            binding.tvNextInstruction.visibility = View.GONE
        } else {
            binding.tvNextInstruction.visibility = View.VISIBLE
            binding.tvNextInstruction.text = paso.descripcion
        }

        // Habilitar/Deshabilitar botones
        binding.btnPrevStep.isEnabled = index > 0
        
        if (index == ruta.pasos.size - 1) {
            binding.btnNextStep.text = "Terminar"
            binding.btnNextStep.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#00C48C"))
        } else {
            binding.btnNextStep.text = "Siguiente"
            binding.btnNextStep.backgroundTintList = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(this, R.color.secundario))
        }

        // Sincronizar listado de pasos integrado
        pasosAdapter.updateData(ruta.pasos, index)
    }

    private fun getIconResource(iconoName: String): Int {
        return when (iconoName) {
            "ic_stairs" -> R.drawable.ic_stairs
            "ic_arrive" -> R.drawable.ic_location_check_destination
            else -> R.drawable.ic_location_current_original
        }
    }

    private fun toggleMapStepsView(showSteps: Boolean) {
        if (isShowingSteps == showSteps) return
        isShowingSteps = showSteps

        val activeView = if (showSteps) binding.rvPasos else binding.cardMap
        val inactiveView = if (showSteps) binding.cardMap else binding.rvPasos

        val duration = 300L

        // 1. Rotate and Scale FAB
        binding.btnMapToggle.animate()
            .scaleX(0f)
            .scaleY(0f)
            .rotationBy(180f)
            .setDuration(duration / 2)
            .withEndAction {
                if (showSteps) {
                    binding.btnMapToggle.setImageResource(R.drawable.ic_map_nav)
                } else {
                    binding.btnMapToggle.setImageResource(R.drawable.ic_go_to_pasos)
                }
                binding.btnMapToggle.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .rotationBy(180f)
                    .setDuration(duration / 2)
                    .start()
            }
            .start()

        // 2. Cross-fade topbar title
        binding.topBar.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                binding.topBar.setTitle(if (showSteps) "Instrucciones" else "Mapa")
                binding.topBar.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()

        // 3. Fade out inactive view, then Fade in active view
        inactiveView.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                inactiveView.visibility = View.GONE
                activeView.alpha = 0f
                activeView.visibility = View.VISIBLE
                activeView.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }
}
