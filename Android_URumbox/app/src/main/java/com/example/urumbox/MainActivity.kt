package com.example.urumbox

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.urumbox.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Búsqueda
        binding.searchBarDashboard.setOnClickListener {
            startActivity(Intent(this, BusquedaActivity::class.java))
        }

        // Emergencias y Evacuación
        binding.btnSimulacroRoute.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }

        binding.btnReportIncident.setOnClickListener {
            startActivity(Intent(this, ReportarEmergenciaActivity::class.java))
        }

        // Objetos Perdidos
        binding.btnAudifonosRoute.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }

        binding.btnAudifonosDetails.setOnClickListener {
            startActivity(Intent(this, ObjetosActivity::class.java))
        }

        binding.btnCelularRoute.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }

        binding.btnCelularDetails.setOnClickListener {
            startActivity(Intent(this, ObjetosActivity::class.java))
        }

        binding.btnAddObject.setOnClickListener {
            startActivity(Intent(this, ObjetosActivity::class.java))
        }

        // Topbar
        binding.btnNavNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        
        binding.btnNavProfile.setOnClickListener {
            startActivity(Intent(this, com.example.urumbox.useractivity.PerfilActivity::class.java))
        }
    }
}