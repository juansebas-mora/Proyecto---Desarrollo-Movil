package com.example.urumbox.emergencyactivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.urumbox.R
import com.example.urumbox.ui.InteractiveMapView

class PuntosEncuentroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puntos_encuentro)

        findViewById<InteractiveMapView>(R.id.interactiveMapView)
            .setRouteData(emptyList(), null)
    }
}