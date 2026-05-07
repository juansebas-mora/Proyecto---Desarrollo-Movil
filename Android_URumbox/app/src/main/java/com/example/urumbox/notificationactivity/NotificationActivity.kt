package com.example.urumbox.notificationactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urumbox.R
import com.example.urumbox.data.model.Notificacion
import com.example.urumbox.databinding.ActivityNotificacionesBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationActivity : AppCompatActivity() {

    private val viewModel: NotificacionViewModel by viewModels()
    private lateinit var binding: ActivityNotificacionesBinding
    private lateinit var adapter: NotificacionAdapter
    private val listaCompleta = mutableListOf<Notificacion>()
    private val listaFiltrada = mutableListOf<Notificacion>()
    private var filtroActual = "Todos"
    private var dialogActivo: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityNotificacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        configurarRecyclerView()
        configurarFiltros()
        configurarFab()
        observarViewModel()
    }

    private fun observarViewModel() {
        viewModel.notificaciones.observe(this) { notificaciones ->
            listaCompleta.clear()
            listaCompleta.addAll(notificaciones)
            aplicarFiltroActual()
        }

        viewModel.estadoCreacion.observe(this) { result ->
            result ?: return@observe
            result.onSuccess {
                dialogActivo?.dismiss()
                dialogActivo = null
                Toast.makeText(this, "Aviso publicado correctamente.", Toast.LENGTH_SHORT).show()
                viewModel.onEstadoCreacionConsumed()
            }.onFailure { error ->
                Toast.makeText(this, "Error al publicar: ${error.message}", Toast.LENGTH_SHORT).show()
                viewModel.onEstadoCreacionConsumed()
            }
        }

        viewModel.error.observe(this) { error ->
            error ?: return@observe
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            viewModel.onErrorConsumed()
        }
    }

    private fun aplicarFiltroActual() {
        adapter.filtrar(filtroActual, listaCompleta)
    }

    private fun iconoPorTipo(tipo: String): Int = when (tipo) {
        "Incidente"          -> R.drawable.ic_warning_white
        "Limpieza"           -> R.drawable.ic_restaurar_white
        "Actividad"          -> R.drawable.ic_group_white
        "Acceso Restringido" -> R.drawable.ic_lock_white
        "Ruta Alternativa"   -> R.drawable.ic_help_circle_white
        else                 -> R.drawable.ic_warning_white
    }

    private fun mostrarDialogDetalles(n: Notificacion) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ver_detalles, null)

        dialogView.findViewById<TextView>(R.id.dialogHora).text = n.hora
        dialogView.findViewById<TextView>(R.id.dialogTipo).text = n.tipo
        dialogView.findViewById<ImageView>(R.id.dialogIcono).setImageResource(n.iconoResId)
        dialogView.findViewById<TextView>(R.id.dialogNombre).text = n.nombreReportante
        dialogView.findViewById<TextView>(R.id.dialogArea).text = n.zonaAfectada
        dialogView.findViewById<ImageView>(R.id.dialogIconoDept).setImageResource(iconoPorTipo(n.tipo))
        dialogView.findViewById<TextView>(R.id.dialogFecha).text = n.fecha
        dialogView.findViewById<TextView>(R.id.dialogUbicacion).text = n.ubicacion
        dialogView.findViewById<TextView>(R.id.dialogAsunto).text = n.descripcion
        dialogView.findViewById<TextView>(R.id.dialogDocumentos).text =
            n.horaExpiracion.ifEmpty { "No especificada" }

        val tvPrioridad = dialogView.findViewById<TextView>(R.id.dialogPrioridad)
        tvPrioridad.text = n.prioridad
        tvPrioridad.setTextColor(getColor(when (n.prioridad) {
            "Alta" -> R.color.rojo_ur
            "Baja" -> R.color.text_bajo
            else   -> R.color.text_medio
        }))

        dialogView.findViewById<TextView>(R.id.dialogCorreo).text =
            when (n.estado) {
                "pendiente" -> "Pendiente de aprobación"
                "activa"    -> "Activa"
                else        -> n.estado
            }

        dialogView.findViewById<TextView>(R.id.dialogTelefono).text =
            if (n.afectaRuta) "Sí — desvíe su trayecto" else "No"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun configurarRecyclerView() {
        adapter = NotificacionAdapter(
            lista = listaFiltrada,
            rolUsuario = "Admin",
            onVerDetalles = { n, _ ->
                n.leida = true
                aplicarFiltroActual()
                mostrarDialogDetalles(n)
            },
            onAceptar = { n, _ ->
                if (n.estado == "pendiente") n.estado = "activa"
                n.leida = true
                aplicarFiltroActual()
                Toast.makeText(this, "Marcado como visto", Toast.LENGTH_SHORT).show()
            },
            onRechazar = { n, _ ->
                n.eliminada = true
                aplicarFiltroActual()
                Toast.makeText(this, "Aviso archivado", Toast.LENGTH_SHORT).show()
            },
            onRestaurar = { n, _ ->
                n.eliminada = false
                aplicarFiltroActual()
                Toast.makeText(this, "Aviso restaurado", Toast.LENGTH_SHORT).show()
            },
            onEliminarDefinitivo = { n, _ ->
                listaCompleta.remove(n)
                aplicarFiltroActual()
                Toast.makeText(this, "Aviso eliminado definitivamente", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvNotificaciones.layoutManager = LinearLayoutManager(this)
        binding.rvNotificaciones.adapter = adapter
    }

    private fun configurarFiltros() {
        val tabs = listOf(
            binding.tabTodos,
            binding.tabSinLeer,
            binding.tabLeidos,
            binding.tabEliminados
        )

        tabs[1].text = getString(R.string.tab_sin_leer)
        tabs[2].text = getString(R.string.tab_leidos)
        tabs[3].text = getString(R.string.tab_eliminados)

        tabs.forEach { tab ->
            tab.setOnClickListener {
                tabs.forEach { t ->
                    t.setBackgroundResource(R.drawable.bg_button_blue_rounded)
                    t.setTextColor(getColor(android.R.color.white))
                }
                tab.setBackgroundResource(R.drawable.bg_button_dark)
                tab.setTextColor(getColor(android.R.color.white))

                filtroActual = tab.text.toString()
                aplicarFiltroActual()
            }
        }
    }

    private fun configurarFab() {
        binding.fabNueva.setOnClickListener { mostrarDialogNuevoAviso() }
    }

    private fun mostrarDialogNuevoAviso() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nueva_notificacion, null)

        val spinnerTipo      = dialogView.findViewById<Spinner>(R.id.spinnerTipo)
        val spinnerPrioridad = dialogView.findViewById<Spinner>(R.id.spinnerPrioridad)
        val etZona           = dialogView.findViewById<EditText>(R.id.etNuevoArea)
        val etDescripcion    = dialogView.findViewById<EditText>(R.id.etNuevoAsunto)
        val etHoraExp        = dialogView.findViewById<EditText>(R.id.etHoraExpiracion)

        val tipos = listOf("Incidente", "Limpieza", "Actividad", "Acceso Restringido", "Ruta Alternativa")
        spinnerTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val prioridades = listOf("Alta", "Media", "Baja")
        spinnerPrioridad.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, prioridades)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerPrioridad.setSelection(1)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogActivo = dialog

        dialogView.findViewById<android.widget.ImageButton>(R.id.btnCerrarDialog)
            .setOnClickListener { dialog.dismiss() }

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCrearNotificacion)
            .setOnClickListener {
                val zona        = etZona.text.toString().trim()
                val descripcion = etDescripcion.text.toString().trim()
                val horaExp     = etHoraExp.text.toString().trim()

                if (zona.isEmpty() || descripcion.isEmpty()) {
                    Toast.makeText(this, "Por favor completa los campos requeridos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val tipo      = spinnerTipo.selectedItem.toString()
                val prioridad = spinnerPrioridad.selectedItem.toString()
                val hora      = SimpleDateFormat("h:mm a", Locale.forLanguageTag("es")).format(Date())
                val fecha     = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es")).format(Date())

                val nueva = Notificacion(
                    hora             = hora,
                    tipo             = tipo,
                    nombreReportante = "Admin",
                    fecha            = fecha,
                    zonaAfectada     = zona,
                    iconoResId       = iconoPorTipo(tipo),
                    descripcion      = descripcion,
                    prioridad        = prioridad,
                    horaExpiracion   = horaExp,
                    rolOrigen        = "Admin",
                    estado           = "activa",
                    afectaRuta       = tipo != "Actividad"
                )
                viewModel.crearNotificacion(nueva)
            }
    }
}
