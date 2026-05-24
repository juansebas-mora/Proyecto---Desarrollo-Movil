package com.example.urumbox.mapasactivity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urumbox.databinding.ActivityBusquedaBinding
import androidx.activity.viewModels

class BusquedaActivity : AppCompatActivity() {

    private val viewModel: BusquedaViewModel by viewModels()
    private lateinit var binding: ActivityBusquedaBinding
    private lateinit var adapter: RutaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        binding = ActivityBusquedaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar RecyclerView
        binding.rvResultados.layoutManager = LinearLayoutManager(this)
        adapter = RutaAdapter(emptyList()) { ruta ->
            val intent = Intent(this, MapaActivity::class.java).apply {
                putExtra("id_ruta", ruta.id_ruta)
            }
            startActivity(intent)
        }
        binding.rvResultados.adapter = adapter

        // Escuchar cambios en la barra de búsqueda
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setQuery(s?.toString() ?: "")
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })

        // Observar rutas filtradas
        viewModel.filteredRutas.observe(this) { rutas ->
            adapter.updateData(rutas)
        }
    }
}
