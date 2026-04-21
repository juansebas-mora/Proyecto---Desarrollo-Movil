package com.example.urumbox.useractivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.urumbox.R

class LoginSActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.btnIngresar).setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }
    }
}