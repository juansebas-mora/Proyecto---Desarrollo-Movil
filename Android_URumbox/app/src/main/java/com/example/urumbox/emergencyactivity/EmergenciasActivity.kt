package com.example.urumbox.emergencyactivity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import com.example.urumbox.R
import com.example.urumbox.ui.InteractiveMapView

class EmergenciasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_emergencias)

        findViewById<com.example.urumbox.TopbarView>(R.id.topBar)
            .setOnBackClickListener { finish() }

        findViewById<CardView>(R.id.btnRutasEvacuacion).setOnClickListener {
            startActivity(Intent(this, RutaEvacuacionActivity::class.java))
        }

        findViewById<CardView>(R.id.btnPuntosEncuentro).setOnClickListener {
            startActivity(Intent(this, PuntosEncuentroActivity::class.java))
        }

        findViewById<AppCompatButton>(R.id.btnReportarEmergencia).setOnClickListener {
            startActivity(Intent(this, ReportarEmergenciaActivity::class.java))
        }

        // Mapa sin ruta, solo muestra el plano
        findViewById<InteractiveMapView>(R.id.interactiveMapView)
            .setRouteData(emptyList(), null)
    }
}