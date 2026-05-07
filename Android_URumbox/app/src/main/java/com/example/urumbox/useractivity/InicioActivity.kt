package com.example.urumbox.useractivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.urumbox.MainActivity
import com.example.urumbox.R
import com.google.firebase.auth.FirebaseAuth

class InicioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_inicio)

        findViewById<Button>(R.id.btnInicioSesion).setOnClickListener {
            startActivity(Intent(this, LoginSActivity::class.java))
        }

        findViewById<Button>(R.id.btnRegistrarse).setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }
}
