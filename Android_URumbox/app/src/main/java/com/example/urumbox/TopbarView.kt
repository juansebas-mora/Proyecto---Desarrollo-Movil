package com.example.urumbox

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.urumbox.useractivity.PerfilActivity

class TopbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var tvTitle: TextView
    private var btnNotification: ImageButton
    private var btnProfile: ImageButton

    init {
        LayoutInflater.from(context).inflate(R.layout.component_topbar, this, true)
        
        tvTitle = findViewById(R.id.tvTitle)
        btnNotification = findViewById(R.id.btnNotification)
        btnProfile = findViewById(R.id.btnProfile)

        // Aplicar atributos personalizados si existen
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TopbarView,
            0, 0
        ).apply {
            try {
                val title = getString(R.styleable.TopbarView_topbarTitle)
                if (title != null) {
                    tvTitle.text = title
                }
            } finally {
                recycle()
            }
        }

        setupNavigation()
    }

    private fun setupNavigation() {
        btnNotification.setOnClickListener {
            val intent = Intent(context, NotificationActivity::class.java)
            context.startActivity(intent)
        }

        btnProfile.setOnClickListener {
            val intent = Intent(context, PerfilActivity::class.java)
            context.startActivity(intent)
        }
    }

    fun setTitle(title: String) {
        tvTitle.text = title
    }
}