package com.example.urumbox

import android.content.Intent
import com.example.urumbox.notificationactivity.NotificationActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.urumbox.databinding.ActivityMainBinding
import com.example.urumbox.objetosactivity.ObjetosActivity
import com.example.urumbox.mapasactivity.BusquedaActivity
import com.example.urumbox.mapasactivity.MapaActivity
import androidx.activity.viewModels

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Búsqueda
        binding.searchBarDashboard.setOnClickListener {
            val intent = Intent(this, BusquedaActivity::class.java)
            val options = androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                binding.searchBarDashboard,
                "search_bar_transition"
            )
            startActivity(intent, options.toBundle())
        }

        // Emergencias y Evacuación
        binding.btnSimulacroRoute.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }

        binding.btnReportIncident.setOnClickListener {
            startActivity(Intent(this, EmergenciasActivity::class.java))
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