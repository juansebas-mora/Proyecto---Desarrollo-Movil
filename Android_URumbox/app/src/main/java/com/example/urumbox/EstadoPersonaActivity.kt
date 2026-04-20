package com.example.urumbox
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EstadoPersonaActivity : AppCompatActivity() {

    private var estadoSeleccionado = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estado_persona)

        val btnAlto   = findViewById<Button>(R.id.btnAlto)
        val btnMedio  = findViewById<Button>(R.id.btnMedio)
        val btnBajo   = findViewById<Button>(R.id.btnBajo)
        val btnEnviar = findViewById<Button>(R.id.btnEnviarEstado)
        val botones   = listOf(btnAlto, btnMedio, btnBajo)

        btnAlto.setOnClickListener {
            estadoSeleccionado = "ALTO"
            resaltarSeleccion(btnAlto, botones)
        }

        btnMedio.setOnClickListener {
            estadoSeleccionado = "MEDIO"
            resaltarSeleccion(btnMedio, botones)
        }

        btnBajo.setOnClickListener {
            estadoSeleccionado = "BAJO"
            resaltarSeleccion(btnBajo, botones)
        }

        btnEnviar.setOnClickListener {
            if (estadoSeleccionado.isEmpty()) {
                Toast.makeText(this, "Selecciona un estado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, ConfirmacionEstadoActivity::class.java)
            intent.putExtra("estado", estadoSeleccionado)
            startActivity(intent)
        }
    }

    private fun resaltarSeleccion(seleccionado: Button, todos: List<Button>) {
        todos.forEach { it.alpha = if (it == seleccionado) 1.0f else 0.4f }
    }
}