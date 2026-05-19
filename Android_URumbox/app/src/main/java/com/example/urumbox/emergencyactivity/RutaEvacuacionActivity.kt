package com.example.urumbox.emergencyactivity

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.urumbox.R

class RutaEvacuacionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruta_evacuacion)

        // Botón volver
        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener {
            finish()
        }

        // Botón SIGA LA RUTA
        findViewById<Button>(R.id.btnSigueLaRuta).setOnClickListener {
            Toast.makeText(this, "Iniciando navegación...", Toast.LENGTH_SHORT).show()
        }
    }
}