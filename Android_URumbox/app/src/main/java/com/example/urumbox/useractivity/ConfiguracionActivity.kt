package com.example.urumbox.useractivity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.urumbox.useractivity.ContactanosActivity
import com.example.urumbox.useractivity.PermisosActivity
import com.example.urumbox.R
import com.example.urumbox.useractivity.VersionActivity

class ConfiguracionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.itemPermisos).setOnClickListener {
            startActivity(Intent(this, PermisosActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.itemCambiarContrasena).setOnClickListener {
            startActivity(Intent(this, CambiarContrasenaActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.itemComentario).setOnClickListener {
            startActivity(Intent(this, ComentarioActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.itemContactanos).setOnClickListener {
            startActivity(Intent(this, ContactanosActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.itemVersion).setOnClickListener {
            startActivity(Intent(this, VersionActivity::class.java))
        }
    }
}