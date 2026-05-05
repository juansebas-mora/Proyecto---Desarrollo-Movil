package com.example.urumbox

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class VerificacionVigilanteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificacion_vigilante)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etContrasena = findViewById<EditText>(R.id.etContrasena)
        val btnVerificar = findViewById<AppCompatButton>(R.id.btnVerificar)
        val tvError = findViewById<TextView>(R.id.tvError)
        val btnVolver = findViewById<ImageButton>(R.id.btnVolver)

        btnVolver.setOnClickListener { finish() }

        btnVerificar.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            // Credenciales temporales hasta conectar Firebase
            val esValido = (usuario == "vigilante" && contrasena == "1234") ||
                    (usuario == "admin" && contrasena == "1234")

            if (esValido) {
                tvError.visibility = View.GONE
                val intent = Intent(this, ReportarEmergenciaActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                tvError.visibility = View.VISIBLE
            }
        }
    }
}