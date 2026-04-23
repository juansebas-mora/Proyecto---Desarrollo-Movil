package com.example.urumbox.useractivity

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.urumbox.R

class PerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        findViewById<LinearLayout>(R.id.itemInfoPersonal).setOnClickListener {
            startActivity(Intent(this, InfoPersonalActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.itemConfiguracion).setOnClickListener {
            startActivity(Intent(this, ConfiguracionActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.itemCerrarSesion).setOnClickListener {
            startActivity(Intent(this, InicioActivity::class.java))
            finishAffinity()
        }
    }
}