package com.example.urumbox.vigilante

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.urumbox.R

class VigilanteMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vigilante_main)

        val spinnerZona = findViewById<Spinner>(R.id.spinnerZona)
        val zonas = listOf("Claustro", "El Tiempo")
        spinnerZona.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, zonas)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }
}
