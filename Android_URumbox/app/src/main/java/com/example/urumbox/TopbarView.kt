package com.example.urumbox

import android.content.Context
import com.example.urumbox.notificationactivity.NotificationActivity
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout

class TopbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var tvTitle: TextView
    private var btnBack: ImageButton
    private var btnNotification: ImageButton
    private var btnProfile: ImageButton

    init {
        LayoutInflater.from(context).inflate(R.layout.component_topbar, this, true)

        tvTitle = findViewById(R.id.tvTitle)
        btnBack = findViewById(R.id.btnBack)
        btnNotification = findViewById(R.id.btnNotification)
        btnProfile = findViewById(R.id.btnProfile)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TopbarView,
            0, 0
        ).apply {
            try {
                val title = getString(R.styleable.TopbarView_topbarTitle)
                if (title != null) tvTitle.text = title

                val showBack = getBoolean(R.styleable.TopbarView_showBackButton, false)
                btnBack.visibility = if (showBack) View.VISIBLE else View.GONE
            } finally {
                recycle()
            }
        }

        setupNavigation()
    }

    private fun setupNavigation() {
        btnBack.setOnClickListener {
            (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
        }

        btnNotification.setOnClickListener {
            context.startActivity(Intent(context, NotificationActivity::class.java))
        }

        btnProfile.setOnClickListener {
            context.startActivity(Intent(context, com.example.urumbox.useractivity.PerfilActivity::class.java))
        }
    }

    fun setTitle(title: String) {
        tvTitle.text = title
    }

    fun setBackButtonVisible(visible: Boolean) {
        btnBack.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun setNotificationButtonVisible(visible: Boolean) {
        btnNotification.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun setOnBackClickListener(listener: OnClickListener) {
        btnBack.setOnClickListener(listener)
    }
}
