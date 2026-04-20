package com.example.urumbox

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RutaEvacuacionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruta_evacuacion)

        // Botón volver
        findViewById<android.widget.Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Botón SIGA LA RUTA
        findViewById<android.widget.Button>(R.id.btnSigueLaRuta).setOnClickListener {
            Toast.makeText(this, "Iniciando navegación...", Toast.LENGTH_SHORT).show()
            // Aquí puedes lanzar Google Maps o tu lógica de navegación
        }
    }
}
