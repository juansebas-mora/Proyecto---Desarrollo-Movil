package com.example.urumbox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ObjetosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_objetos)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_objetos, ObjetosPerdidosFragment())
                .commit()
        }
    }
}
