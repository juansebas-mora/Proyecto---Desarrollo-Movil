package com.example.urumbox

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etNombre  = findViewById<EditText>(R.id.etLoginNombre)
        val toggleRol = findViewById<MaterialButtonToggleGroup>(R.id.toggleRol)
        val btnIngresar = findViewById<MaterialButton>(R.id.btnIngresar)

        // Visitante seleccionado por defecto
        toggleRol.check(R.id.btnRolVisitante)

        btnIngresar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            UsuarioSesion.nombre = nombre
            UsuarioSesion.rol = when (toggleRol.checkedButtonId) {
                R.id.btnRolOperador -> "Operador"
                R.id.btnRolAdmin    -> "Admin"
                else                -> "Visitante"
            }

            startActivity(Intent(this, NotificationActivity::class.java))
            finish()
        }
    }
}
