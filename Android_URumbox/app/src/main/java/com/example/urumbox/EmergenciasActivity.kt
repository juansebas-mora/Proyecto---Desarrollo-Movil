package com.example.urumbox

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView

class EmergenciasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_emergencias)

        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener { finish() }

        findViewById<CardView>(R.id.btnRutasEvacuacion).setOnClickListener {
            startActivity(Intent(this, RutaEvacuacionActivity::class.java))
        }

        findViewById<CardView>(R.id.btnPuntosEncuentro).setOnClickListener {
            startActivity(Intent(this, PuntosEncuentroActivity::class.java))
        }

        findViewById<AppCompatButton>(R.id.btnReportarEmergencia).setOnClickListener {
            startActivity(Intent(this, VerificacionVigilanteActivity::class.java))
        }
    }
}