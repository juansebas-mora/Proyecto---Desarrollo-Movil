package com.example.urumbox

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Botón: Rutas de evacuación
        findViewById<CardView>(R.id.btnRutasEvacuacion)
            .setOnClickListener {
                startActivity(Intent(this, RutaEvacuacionActivity::class.java))
            }

        // Botón: Puntos de encuentro
        findViewById<CardView>(R.id.btnPuntosEncuentro)
            .setOnClickListener {
                startActivity(Intent(this, PuntosEncuentroActivity::class.java))
            }

        // Botón: ¿Cómo te encuentras?
        findViewById<CardView>(R.id.btnComoTeEncuentras)
            .setOnClickListener {
                startActivity(Intent(this, EstadoPersonaActivity::class.java))
            }

        // Botón: Reportar emergencia
        findViewById<CardView>(R.id.btnReportarEmergencia)
            .setOnClickListener {
                startActivity(Intent(this, ReportarEmergenciaActivity::class.java))
            }
    }
}