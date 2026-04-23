package com.example.urumbox

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class AccessMainActivity : AppCompatActivity() {

    private val viewModel: AccessMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_main)

        findViewById<ImageButton>(R.id.btnViewAllHistory).setOnClickListener {
            startActivity(Intent(this, AccessHistoryActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnAddVisitor).setOnClickListener {
            startActivity(Intent(this, AccessRequestActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnQrCode).setOnClickListener {
            startActivity(Intent(this, AccessQrActivity::class.java))
        }
    }
}
