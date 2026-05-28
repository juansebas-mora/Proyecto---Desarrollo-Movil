package com.example.urumbox.vigilante

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.urumbox.R
import com.example.urumbox.accessactivity.QrScannerActivity
import com.example.urumbox.databinding.ActivityVigilanteMainBinding

class VigilanteMainActivity : AppCompatActivity() {

    private val viewModel: VigilanteMainViewModel by viewModels()
    private lateinit var binding: ActivityVigilanteMainBinding

    private val zonas = listOf("Seleccionar zona", "Claustro", "El Tiempo")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVigilanteMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topBar.setNotificationButtonVisible(false)

        val adapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, zonas
        ) {
            override fun isEnabled(position: Int) = position != 0
            override fun getDropDownView(pos: Int, cv: View?, parent: ViewGroup): View =
                super.getDropDownView(pos, cv, parent).also { v ->
                    (v as? TextView)?.setTextColor(
                        if (pos == 0) ContextCompat.getColor(context, R.color.texto_secundario)
                        else ContextCompat.getColor(context, R.color.texto_principal)
                    )
                }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerZona.adapter = adapter

        binding.spinnerZona.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos != 0) viewModel.onZonaSelected()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.cardQr.setOnClickListener {
            val pos = binding.spinnerZona.selectedItemPosition
            val zona = if (pos > 0) zonas[pos] else null
            viewModel.onCardQrClicked(zona)
        }

        viewModel.zonaError.observe(this) { hasError ->
            binding.tvZonaError.visibility = if (hasError) View.VISIBLE else View.GONE
        }

        viewModel.navigateToScanner.observe(this) { zona ->
            zona ?: return@observe
            startActivity(Intent(this, QrScannerActivity::class.java).apply {
                putExtra(QrScannerActivity.EXTRA_ZONA, zona)
            })
            viewModel.onNavigationConsumed()
        }
    }
}
