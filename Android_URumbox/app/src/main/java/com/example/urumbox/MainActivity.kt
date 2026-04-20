package com.example.urumbox
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Botón: Rutas de evacuación
        findViewById<androidx.cardview.widget.CardView>(R.id.btnRutasEvacuacion)
            .setOnClickListener {
                startActivity(Intent(this, RutaEvacuacionActivity::class.java))
            }

        // Botón: Puntos de encuentro
        findViewById<androidx.cardview.widget.CardView>(R.id.btnPuntosEncuentro)
            .setOnClickListener {
                startActivity(Intent(this, PuntosEncuentroActivity::class.java))
            }

        // Botón: ¿Cómo te encuentras?
        findViewById<androidx.cardview.widget.CardView>(R.id.btnComoTeEncuentras)
            .setOnClickListener {
                startActivity(Intent(this, EstadoPersonaActivity::class.java))
            }
    }
}