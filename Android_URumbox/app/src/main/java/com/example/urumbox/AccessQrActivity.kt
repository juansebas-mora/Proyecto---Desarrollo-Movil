package com.example.urumbox

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class AccessQrActivity : AppCompatActivity() {

    private val viewModel: AccessQrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_qr)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }
}
