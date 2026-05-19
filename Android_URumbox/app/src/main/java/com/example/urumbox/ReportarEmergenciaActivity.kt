package com.example.urumbox

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ReportarEmergenciaActivity : AppCompatActivity() {

    private var categoriaSeleccionada = ""
    private var gravedadSeleccionada = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportar_emergencia)

        // Botón volver
        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener { finish() }

        // Botones categoría
        val btnIncendio = findViewById<AppCompatButton>(R.id.btnIncendio)
        val btnMedica   = findViewById<AppCompatButton>(R.id.btnMedica)
        val btnRobo     = findViewById<AppCompatButton>(R.id.btnRobo)
        val btnSismo    = findViewById<AppCompatButton>(R.id.btnSismo)
        val btnOtro     = findViewById<AppCompatButton>(R.id.btnOtro)
        val botonesCategoria = listOf(btnIncendio, btnMedica, btnRobo, btnSismo, btnOtro)

        // Botones gravedad
        val btnBaja  = findViewById<AppCompatButton>(R.id.btnBaja)
        val btnMedia = findViewById<AppCompatButton>(R.id.btnMedia)
        val btnAlta  = findViewById<AppCompatButton>(R.id.btnAlta)
        val botonesGravedad = listOf(btnBaja, btnMedia, btnAlta)

        // Otros elementos
        val etDescripcion      = findViewById<EditText>(R.id.etDescripcion)
        val btnSeleccionarMapa = findViewById<LinearLayout>(R.id.btnSeleccionarMapa)
        val btnSubirFoto       = findViewById<LinearLayout>(R.id.btnSubirFoto)
        val btnEnviarReporte   = findViewById<AppCompatButton>(R.id.btnEnviarReporte)

        // Selección categoría
        btnIncendio.setOnClickListener { categoriaSeleccionada = "Incendio"; resaltarSeleccion(btnIncendio, botonesCategoria) }
        btnMedica.setOnClickListener   { categoriaSeleccionada = "Médica";   resaltarSeleccion(btnMedica, botonesCategoria) }
        btnRobo.setOnClickListener     { categoriaSeleccionada = "Robo";     resaltarSeleccion(btnRobo, botonesCategoria) }
        btnSismo.setOnClickListener    { categoriaSeleccionada = "Sismo";    resaltarSeleccion(btnSismo, botonesCategoria) }
        btnOtro.setOnClickListener     { categoriaSeleccionada = "Otro";     resaltarSeleccion(btnOtro, botonesCategoria) }

        // Selección gravedad
        btnBaja.setOnClickListener  { gravedadSeleccionada = "Baja";  resaltarSeleccion(btnBaja, botonesGravedad) }
        btnMedia.setOnClickListener { gravedadSeleccionada = "Media"; resaltarSeleccion(btnMedia, botonesGravedad) }
        btnAlta.setOnClickListener  { gravedadSeleccionada = "Alta";  resaltarSeleccion(btnAlta, botonesGravedad) }

        // Mapa y foto
        btnSeleccionarMapa.setOnClickListener {
            Toast.makeText(this, "Seleccionar ubicación en mapa", Toast.LENGTH_SHORT).show()
        }
        btnSubirFoto.setOnClickListener {
            Toast.makeText(this, "Subir foto", Toast.LENGTH_SHORT).show()
        }

        // Enviar reporte
        btnEnviarReporte.setOnClickListener {
            when {
                categoriaSeleccionada.isEmpty() ->
                    Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
                gravedadSeleccionada.isEmpty() ->
                    Toast.makeText(this, "Selecciona el nivel de gravedad", Toast.LENGTH_SHORT).show()
                etDescripcion.text.toString().trim().isEmpty() ->
                    Toast.makeText(this, "Escribe una descripción", Toast.LENGTH_SHORT).show()
                else -> mostrarDialogoExito()
            }
        }
    }

    private fun mostrarDialogoExito() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_reporte_publicado, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Fondo transparente para que se vean las esquinas redondeadas
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<AppCompatButton>(R.id.btnCerrarDialog).setOnClickListener {
            dialog.dismiss()
            finish() // Vuelve a la pantalla anterior
        }

        dialog.show()
    }

    private fun resaltarSeleccion(seleccionado: AppCompatButton, todos: List<AppCompatButton>) {
        todos.forEach { btn ->
            if (btn == seleccionado) {
                btn.setBackgroundResource(R.drawable.rounded_button_background)
                btn.setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                btn.setBackgroundResource(R.drawable.chip_unselected_bg)
                btn.setTextColor(resources.getColor(android.R.color.holo_blue_dark, null))
            }
        }
    }
}