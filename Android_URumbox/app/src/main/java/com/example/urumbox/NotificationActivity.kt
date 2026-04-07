package com.example.urumbox

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationActivity : AppCompatActivity() {

    private lateinit var adapter: NotificacionAdapter
    private val listaCompleta = mutableListOf<Notificacion>()
    private val listaFiltrada = mutableListOf<Notificacion>()
    private var filtroActual = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si no hay sesión activa, ir al login
        if (!UsuarioSesion.estaLogueado) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_notificaciones)

        cargarDatosDummy()
        configurarRecyclerView()
        configurarFiltros()
        configurarBottomNav()
        configurarFab()
        configurarPerfil()
    }

    private fun cargarDatosDummy() {
        listaCompleta.addAll(listOf(
            Notificacion(
                id = 1, hora = "9:15 a.m.", tipo = "Limpieza",
                nombreReportante = "Personal de Mantenimiento",
                fecha = "7 de Abril de 2026", zonaAfectada = "Pasillo B - Piso 2",
                iconoResId = R.drawable.ic_restaurar,
                descripcion = "Piso mojado en proceso de secado. Use la ruta alterna por escaleras C.",
                ubicacion = "Pasillo B entre oficinas 201–215",
                prioridad = "Media", horaExpiracion = "11:00 a.m.",
                rolOrigen = "Operador", afectaRuta = true
            ),
            Notificacion(
                id = 2, hora = "10:30 a.m.", tipo = "Actividad",
                nombreReportante = "Depto. Eventos",
                fecha = "7 de Abril de 2026", zonaAfectada = "Auditorio Principal",
                iconoResId = R.drawable.ic_group,
                descripcion = "Conferencia en curso. El acceso al auditorio está restringido hasta las 12:00.",
                ubicacion = "Edificio Central, Piso 1",
                prioridad = "Baja", horaExpiracion = "12:00 p.m.",
                rolOrigen = "Admin", afectaRuta = false
            ),
            Notificacion(
                id = 3, hora = "8:00 a.m.", tipo = "Incidente",
                nombreReportante = "Guardia de Seguridad",
                fecha = "7 de Abril de 2026", zonaAfectada = "Entrada Principal Norte",
                iconoResId = R.drawable.ic_warning,
                descripcion = "Fuga menor de agua reparada. Zona despejada pero superficie resbaladiza.",
                ubicacion = "Puerta norte, acceso vehicular",
                prioridad = "Alta", horaExpiracion = "Por confirmar",
                rolOrigen = "Operador", estado = "activa", afectaRuta = true
            ),
            Notificacion(
                id = 4, hora = "11:45 a.m.", tipo = "Acceso Restringido",
                nombreReportante = "Administración",
                fecha = "7 de Abril de 2026", zonaAfectada = "Zona de Servidores - Sótano",
                iconoResId = R.drawable.ic_lock,
                descripcion = "Mantenimiento programado de infraestructura. Solo personal autorizado.",
                ubicacion = "Sótano, puerta S-3",
                prioridad = "Alta", horaExpiracion = "6:00 p.m.",
                rolOrigen = "Admin", afectaRuta = false
            ),
            Notificacion(
                id = 5, hora = "12:00 p.m.", tipo = "Incidente",
                nombreReportante = "Ana García",
                fecha = "7 de Abril de 2026", zonaAfectada = "Escaleras Torre A",
                iconoResId = R.drawable.ic_warning,
                descripcion = "Escaleras principales bloqueadas por caja de materiales. Use el ascensor o escaleras B.",
                ubicacion = "Torre A, entre pisos 1 y 3",
                prioridad = "Media", horaExpiracion = "5:00 p.m.",
                rolOrigen = "Visitante", estado = "pendiente", afectaRuta = true
            ),
            Notificacion(
                id = 6, hora = "1:30 p.m.", tipo = "Ruta Alternativa",
                nombreReportante = "Operaciones",
                fecha = "7 de Abril de 2026", zonaAfectada = "Corredor Central",
                iconoResId = R.drawable.ic_help_circle,
                descripcion = "Corredor central cerrado por evento corporativo. Acceda por corredor lateral este.",
                ubicacion = "Planta baja, entre bloques A y B",
                prioridad = "Media", horaExpiracion = "4:00 p.m.",
                rolOrigen = "Operador", afectaRuta = true
            )
        ))
        listaFiltrada.addAll(listaCompleta)
    }

    private fun aplicarFiltroActual() {
        adapter.filtrar(filtroActual, listaCompleta)
    }

    private fun iconoPorTipo(tipo: String): Int = when (tipo) {
        "Incidente"          -> R.drawable.ic_warning
        "Limpieza"           -> R.drawable.ic_restaurar
        "Actividad"          -> R.drawable.ic_group
        "Acceso Restringido" -> R.drawable.ic_lock
        "Ruta Alternativa"   -> R.drawable.ic_help_circle
        else                 -> R.drawable.ic_warning
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
            "Alta" -> R.color.danger
            "Baja" -> R.color.success
            else   -> R.color.warning
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
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
    }

    private fun configurarRecyclerView() {
        adapter = NotificacionAdapter(
            lista = listaFiltrada,
            rolUsuario = UsuarioSesion.rol,
            onVerDetalles = { n, _ ->
                n.leida = true
                aplicarFiltroActual()
                mostrarDialogDetalles(n)
            },
            onAceptar = { n, _ ->
                if (n.estado == "pendiente") {
                    n.estado = "activa"
                    n.leida = true
                    aplicarFiltroActual()
                    Toast.makeText(this, "Aviso aprobado y publicado", Toast.LENGTH_SHORT).show()
                } else {
                    n.eliminada = true
                    aplicarFiltroActual()
                    Toast.makeText(this, "Marcado como resuelto", Toast.LENGTH_SHORT).show()
                }
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

        val rv = findViewById<RecyclerView>(R.id.rvNotificaciones)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun configurarFiltros() {
        val tabs = listOf<TextView>(
            findViewById(R.id.tabTodos),
            findViewById(R.id.tabSinLeer),
            findViewById(R.id.tabLeidos),
            findViewById(R.id.tabEliminados)
        )

        // Actualizar etiquetas de tabs según strings actualizados
        tabs[1].text = getString(R.string.tab_sin_leer)
        tabs[2].text = getString(R.string.tab_leidos)
        tabs[3].text = getString(R.string.tab_eliminados)

        tabs.forEach { tab ->
            tab.setOnClickListener {
                tabs.forEach { t ->
                    t.setBackgroundResource(R.drawable.bg_tab_unselected)
                    t.setTextColor(getColor(android.R.color.white))
                }
                tab.setBackgroundResource(R.drawable.bg_tab_selected)
                tab.setTextColor(getColor(android.R.color.white))

                filtroActual = tab.text.toString()
                aplicarFiltroActual()
            }
        }
    }

    private fun configurarBottomNav() {
        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            Toast.makeText(this, "Inicio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarFab() {
        val fab = findViewById<FloatingActionButton>(R.id.fabNueva)
        fab.setOnClickListener { mostrarDialogNuevoAviso() }
    }

    private fun configurarPerfil() {
        findViewById<ImageButton>(R.id.btnPerfil).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("${UsuarioSesion.nombre} (${UsuarioSesion.rol})")
                .setMessage("¿Deseas cerrar sesión?")
                .setPositiveButton("Cerrar sesión") { _, _ ->
                    UsuarioSesion.cerrarSesion()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun tiposPorRol(): List<String> = when (UsuarioSesion.rol) {
        "Visitante" -> listOf("Incidente")
        "Operador"  -> listOf("Incidente", "Limpieza", "Actividad", "Ruta Alternativa")
        "Admin"     -> listOf("Incidente", "Limpieza", "Actividad", "Acceso Restringido", "Ruta Alternativa")
        else        -> listOf("Incidente")
    }

    private fun mostrarDialogNuevoAviso() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nueva_notificacion, null)

        val spinnerTipo      = dialogView.findViewById<Spinner>(R.id.spinnerTipo)
        val spinnerPrioridad = dialogView.findViewById<Spinner>(R.id.spinnerPrioridad)
        val etZona           = dialogView.findViewById<EditText>(R.id.etNuevoArea)
        val etDescripcion    = dialogView.findViewById<EditText>(R.id.etNuevoAsunto)
        val etHoraExp        = dialogView.findViewById<EditText>(R.id.etHoraExpiracion)

        val tipos = tiposPorRol()
        spinnerTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val prioridades = listOf("Alta", "Media", "Baja")
        spinnerPrioridad.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, prioridades)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerPrioridad.setSelection(1)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

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
                val hora      = SimpleDateFormat("h:mm a", Locale("es")).format(Date())
                val fecha     = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es")).format(Date())

                // Visitantes crean avisos pendientes de aprobación
                val estado = if (UsuarioSesion.rol == "Visitante") "pendiente" else "activa"
                val afectaRuta = tipo != "Actividad"

                val nueva = Notificacion(
                    id               = listaCompleta.size + 1,
                    hora             = hora,
                    tipo             = tipo,
                    nombreReportante = UsuarioSesion.nombre,
                    fecha            = fecha,
                    zonaAfectada     = zona,
                    iconoResId       = iconoPorTipo(tipo),
                    descripcion      = descripcion,
                    prioridad        = prioridad,
                    horaExpiracion   = horaExp,
                    rolOrigen        = UsuarioSesion.rol,
                    estado           = estado,
                    afectaRuta       = afectaRuta
                )
                listaCompleta.add(nueva)
                aplicarFiltroActual()
                dialog.dismiss()

                val msg = if (estado == "pendiente")
                    "Aviso enviado. Pendiente de aprobación."
                else
                    "Aviso publicado correctamente."
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
    }
}
