package com.example.urumbox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ReportarEmergenciaActivity : AppCompatActivity() {

    private var tipoEmergenciaSeleccionado = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportar_emergencia)

        val btnIncendio         = findViewById<CardView>(R.id.btnIncendio)
        val btnSismo            = findViewById<CardView>(R.id.btnSismo)
        val btnEmergenciaMedica = findViewById<CardView>(R.id.btnEmergenciaMedica)
        val btnAmenaza          = findViewById<CardView>(R.id.btnAmenazaSeguridad)
        val btnEnviar           = findViewById<Button>(R.id.btnEnviarAlerta)
        val botones             = listOf(btnIncendio, btnSismo, btnEmergenciaMedica, btnAmenaza)

        // Botón volver
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        // Selección de tipo de emergencia
        btnIncendio.setOnClickListener {
            tipoEmergenciaSeleccionado = "Incendio"
            resaltarSeleccion(btnIncendio, botones)
        }

        btnSismo.setOnClickListener {
            tipoEmergenciaSeleccionado = "Sismo"
            resaltarSeleccion(btnSismo, botones)
        }

        btnEmergenciaMedica.setOnClickListener {
            tipoEmergenciaSeleccionado = "Emergencia médica"
            resaltarSeleccion(btnEmergenciaMedica, botones)
        }

        btnAmenaza.setOnClickListener {
            tipoEmergenciaSeleccionado = "Amenaza de seguridad"
            resaltarSeleccion(btnAmenaza, botones)
        }

        // Enviar alerta
        btnEnviar.setOnClickListener {
            if (tipoEmergenciaSeleccionado.isEmpty()) {
                Toast.makeText(this, "Selecciona un tipo de emergencia", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, ConfirmacionActivity::class.java)
            intent.putExtra("tipo_emergencia", tipoEmergenciaSeleccionado)
            startActivity(intent)
        }
    }

    private fun resaltarSeleccion(seleccionado: CardView, todos: List<CardView>) {
        todos.forEach { it.alpha = if (it == seleccionado) 1.0f else 0.5f }
    }
}
