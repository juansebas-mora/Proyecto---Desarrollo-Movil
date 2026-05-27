package com.example.urumbox.notificationactivity

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotificationActivity : AppCompatActivity() {

    private val viewModel: NotificacionViewModel by viewModels()
    private lateinit var binding: ActivityNotificacionesBinding
    private lateinit var adapter: NotificacionAdapter
    private val listaCompleta = mutableListOf<Notificacion>()
    private val listaFiltrada = mutableListOf<Notificacion>()
    private var filtroActual = "Todos"
    private var dialogActivo: AlertDialog? = null

    private val auth = FirebaseAuth.getInstance()

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
        configurarBotonNueva()
        observarViewModel()

        val uid = auth.currentUser?.uid ?: ""
        viewModel.cargarRolUsuario(uid)
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

        viewModel.estadoActualizacion.observe(this) { result ->
            result ?: return@observe
            result.onSuccess {
                dialogActivo?.dismiss()
                dialogActivo = null
                Toast.makeText(this, "Aviso actualizado correctamente.", Toast.LENGTH_SHORT).show()
                viewModel.onEstadoActualizacionConsumed()
            }.onFailure { error ->
                Toast.makeText(this, "Error al actualizar: ${error.message}", Toast.LENGTH_SHORT).show()
                viewModel.onEstadoActualizacionConsumed()
            }
        }

        viewModel.rolUsuario.observe(this) { rol ->
            adapter.setRol(rol)
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

    private fun formatearHora(timestamp: Timestamp?): String =
        timestamp?.let {
            SimpleDateFormat("h:mm a", Locale.forLanguageTag("es"))
                .apply { timeZone = java.util.TimeZone.getTimeZone("America/Bogota") }
                .format(it.toDate())
        } ?: "Sin hora"

    private fun formatearFecha(timestamp: Timestamp?): String =
        timestamp?.let {
            SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"))
                .apply { timeZone = java.util.TimeZone.getTimeZone("America/Bogota") }
                .format(it.toDate())
        } ?: "Sin fecha"

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

        dialogView.findViewById<TextView>(R.id.dialogHora).text = formatearHora(n.timestamp)
        dialogView.findViewById<TextView>(R.id.dialogTipo).text = n.tipo
        dialogView.findViewById<ImageView>(R.id.dialogIcono).setImageResource(n.iconoResId)
        dialogView.findViewById<TextView>(R.id.dialogNombre).text = n.nombreReportante
        dialogView.findViewById<TextView>(R.id.dialogArea).text = n.zonaAfectada
        dialogView.findViewById<ImageView>(R.id.dialogIconoDept).setImageResource(iconoPorTipo(n.tipo))
        dialogView.findViewById<TextView>(R.id.dialogFecha).text = formatearFecha(n.timestamp)
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

        dialogView.findViewById<ImageButton>(R.id.btnCerrarDetalles)
            .setOnClickListener { dialog.dismiss() }
    }

    private fun configurarRecyclerView() {
        val uidActual = auth.currentUser?.uid ?: ""

        adapter = NotificacionAdapter(
            lista = listaFiltrada,
            rolUsuario = "Visitante",
            uidUsuarioActual = uidActual,
            onVerDetalles = { n, _ ->
                viewModel.marcarLeida(n.id, n.estado)
                mostrarDialogDetalles(n)
            },
            onAceptar = { n, _ ->
                viewModel.marcarLeida(n.id, n.estado)
                Toast.makeText(this, "Marcado como visto", Toast.LENGTH_SHORT).show()
            },
            onRechazar = { n, _ ->
                viewModel.archivarNotificacion(n.id)
                Toast.makeText(this, "Aviso archivado", Toast.LENGTH_SHORT).show()
            },
            onRestaurar = { n, _ ->
                viewModel.restaurarNotificacion(n.id)
                Toast.makeText(this, "Aviso restaurado", Toast.LENGTH_SHORT).show()
            },
            onEliminarDefinitivo = { n, _ ->
                viewModel.eliminarNotificacion(n.id)
                Toast.makeText(this, "Aviso eliminado definitivamente", Toast.LENGTH_SHORT).show()
            },
            onEditar = { n, _ ->
                mostrarDialogEditarAviso(n)
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

    private fun configurarBotonNueva() {
        binding.btnNueva.setOnClickListener { mostrarDialogNuevoAviso() }
    }

    private fun nombreUsuarioActual(): String {
        val user = auth.currentUser ?: return "Usuario"
        return user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore('@')?.takeIf { it.isNotBlank() }
            ?: "Usuario"
    }

    private fun configurarSelectorHora(container: View, etHora: EditText) {
        val listener = View.OnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                ContextThemeWrapper(this, R.style.TimePickerTheme),
                { _, hourOfDay, minute ->
                    val cal2 = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                    }
                    etHora.setText(
                        SimpleDateFormat("h:mm a", Locale.forLanguageTag("es")).format(cal2.time)
                    )
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
            ).show()
        }
        container.setOnClickListener(listener)
        etHora.setOnClickListener(listener)
    }

    private fun mostrarDialogNuevoAviso() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nueva_notificacion, null)

        val spinnerTipo      = dialogView.findViewById<Spinner>(R.id.spinnerTipo)
        val spinnerPrioridad = dialogView.findViewById<Spinner>(R.id.spinnerPrioridad)
        val etZona           = dialogView.findViewById<EditText>(R.id.etNuevoArea)
        val etUbicacion      = dialogView.findViewById<EditText>(R.id.etNuevaUbicacion)
        val etDescripcion    = dialogView.findViewById<EditText>(R.id.etNuevoAsunto)
        val etHoraExp        = dialogView.findViewById<EditText>(R.id.etHoraExpiracion)

        val tipos = listOf("Incidente", "Limpieza", "Actividad", "Acceso Restringido", "Ruta Alternativa")
        spinnerTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val prioridades = listOf("Alta", "Media", "Baja")
        spinnerPrioridad.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, prioridades)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerPrioridad.setSelection(1)

        val llHora = dialogView.findViewById<LinearLayout>(R.id.llHoraExpiracion)
        configurarSelectorHora(llHora, etHoraExp)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogActivo = dialog

        dialogView.findViewById<ImageButton>(R.id.btnCerrarDialog)
            .setOnClickListener { dialog.dismiss() }

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCrearNotificacion)
            .setOnClickListener {
                val zona        = etZona.text.toString().trim()
                val ubicacion   = etUbicacion.text.toString().trim()
                val descripcion = etDescripcion.text.toString().trim()
                val horaExp     = etHoraExp.text.toString().trim()

                if (zona.isEmpty() || descripcion.isEmpty()) {
                    Toast.makeText(this, "Por favor completa los campos requeridos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val tipo      = spinnerTipo.selectedItem.toString()
                val prioridad = spinnerPrioridad.selectedItem.toString()
                val nombre    = nombreUsuarioActual()
                val uid       = auth.currentUser?.uid ?: ""

                val nueva = Notificacion(
                    timestamp        = Timestamp.now(),
                    tipo             = tipo,
                    nombreReportante = nombre,
                    zonaAfectada     = zona,
                    iconoResId       = iconoPorTipo(tipo),
                    ubicacion        = ubicacion,
                    descripcion      = descripcion,
                    prioridad        = prioridad,
                    horaExpiracion   = horaExp,
                    rolOrigen        = "Admin",
                    estado           = "activa",
                    afectaRuta       = tipo != "Actividad",
                    uidCreador       = uid
                )
                viewModel.crearNotificacion(nueva)
            }
    }

    private fun mostrarDialogEditarAviso(notificacion: Notificacion) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nueva_notificacion, null)

        dialogView.findViewById<TextView>(R.id.tvDialogTitle).text = "Editar aviso"
        dialogView.findViewById<TextView>(R.id.tvDialogSubtitle).text = "Modifica los campos del aviso"

        val spinnerTipo      = dialogView.findViewById<Spinner>(R.id.spinnerTipo)
        val spinnerPrioridad = dialogView.findViewById<Spinner>(R.id.spinnerPrioridad)
        val etZona           = dialogView.findViewById<EditText>(R.id.etNuevoArea)
        val etUbicacion      = dialogView.findViewById<EditText>(R.id.etNuevaUbicacion)
        val etDescripcion    = dialogView.findViewById<EditText>(R.id.etNuevoAsunto)
        val etHoraExp        = dialogView.findViewById<EditText>(R.id.etHoraExpiracion)

        val tipos = listOf("Incidente", "Limpieza", "Actividad", "Acceso Restringido", "Ruta Alternativa")
        spinnerTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerTipo.setSelection(tipos.indexOf(notificacion.tipo).coerceAtLeast(0))

        val prioridades = listOf("Alta", "Media", "Baja")
        spinnerPrioridad.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, prioridades)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerPrioridad.setSelection(prioridades.indexOf(notificacion.prioridad).coerceAtLeast(0))

        etZona.setText(notificacion.zonaAfectada)
        etUbicacion.setText(notificacion.ubicacion)
        etDescripcion.setText(notificacion.descripcion)
        etHoraExp.setText(notificacion.horaExpiracion)

        val llHora = dialogView.findViewById<LinearLayout>(R.id.llHoraExpiracion)
        configurarSelectorHora(llHora, etHoraExp)

        val btnPublicar = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCrearNotificacion)
        btnPublicar.text = "Actualizar aviso"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogActivo = dialog

        dialogView.findViewById<ImageButton>(R.id.btnCerrarDialog)
            .setOnClickListener { dialog.dismiss() }

        btnPublicar.setOnClickListener {
            val zona        = etZona.text.toString().trim()
            val ubicacion   = etUbicacion.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val horaExp     = etHoraExp.text.toString().trim()

            if (zona.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(this, "Por favor completa los campos requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tipo      = spinnerTipo.selectedItem.toString()
            val prioridad = spinnerPrioridad.selectedItem.toString()

            val campos = mapOf(
                "tipo"           to tipo,
                "zonaAfectada"   to zona,
                "ubicacion"      to ubicacion,
                "descripcion"    to descripcion,
                "prioridad"      to prioridad,
                "horaExpiracion" to horaExp,
                "afectaRuta"     to (tipo != "Actividad")
            )
            viewModel.actualizarNotificacion(notificacion.id, campos)
        }
    }
}
