package com.example.urumbox

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.urumbox.databinding.ActivityAccessMainBinding


class AccessMainActivity : AppCompatActivity() {

    private val viewModel: AccessMainViewModel by viewModels()
    private lateinit var binding: ActivityAccessMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )
        binding = ActivityAccessMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnViewAllHistory.setOnClickListener {
            startActivity(Intent(this, AccessHistoryActivity::class.java))
        }

        binding.btnAddVisitor.setOnClickListener {
            startActivity(Intent(this, AccessRequestActivity::class.java))
        }

        binding.btnQrCode.setOnClickListener {
            startActivity(Intent(this, AccessQrActivity::class.java))
        }
    }
}
