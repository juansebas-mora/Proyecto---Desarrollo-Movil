package com.example.urumbox.objetosactivity

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.urumbox.R
import com.example.urumbox.databinding.ActivityObjetosBinding

class ObjetosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityObjetosBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        val systemBarColor = getColor(R.color.azul_ur)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        super.onCreate(savedInstanceState)

        binding = ActivityObjetosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container_objetos,
                    ObjetosPerdidosFragment()
                )
                .commit()
        }
    }
}