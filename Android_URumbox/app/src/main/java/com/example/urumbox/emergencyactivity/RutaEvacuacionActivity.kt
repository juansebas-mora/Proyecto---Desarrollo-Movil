package com.example.urumbox.emergencyactivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.urumbox.R
import com.example.urumbox.mapasactivity.MapaActivity
import com.example.urumbox.ui.InteractiveMapView

class RutaEvacuacionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruta_evacuacion)

        // Mapa sin ruta por ahora
        findViewById<InteractiveMapView>(R.id.interactiveMapView)
            .setRouteData(emptyList(), null)

        // Botón SIGA LA RUTA abre MapaActivity con la ruta completa
        findViewById<Button>(R.id.btnSigueLaRuta).setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            intent.putExtra("id_ruta", "ruta_claustro_test")
            startActivity(intent)
        }
    }
}