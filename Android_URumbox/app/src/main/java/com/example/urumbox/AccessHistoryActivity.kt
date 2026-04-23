package com.example.urumbox

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class AccessHistoryActivity : AppCompatActivity() {

    private val viewModel: AccessHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_history)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }
}
