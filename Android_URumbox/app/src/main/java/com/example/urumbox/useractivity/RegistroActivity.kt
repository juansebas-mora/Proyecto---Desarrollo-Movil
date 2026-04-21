package com.example.urumbox.useractivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.urumbox.R

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        findViewById<Button>(R.id.btnRegistrarse).setOnClickListener {
            startActivity(Intent(this, LoginSActivity::class.java))
        }
    }
}