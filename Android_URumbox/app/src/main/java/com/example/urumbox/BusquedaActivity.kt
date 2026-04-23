package com.example.urumbox

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.urumbox.databinding.ActivityBusquedaBinding

class BusquedaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBusquedaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBusquedaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Manejar insets básicos (pantalla completa)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navegación hacia el Mapa
        binding.btnResult1ToMap.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }

        binding.btnResult2ToMap.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }
    }
}
