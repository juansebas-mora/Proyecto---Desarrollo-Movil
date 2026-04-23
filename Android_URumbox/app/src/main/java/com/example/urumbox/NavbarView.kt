package com.example.urumbox

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.LayoutInflater

class NavbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val btnHome: ImageButton
    private val btnBox: ImageButton
    private val btnAccess: ImageButton
    private val btnEmergency: ImageButton

    init {
        LayoutInflater.from(context).inflate(R.layout.navbar_content, this, true)
        setBackgroundResource(R.drawable.bg_navbar_rounded)

        btnHome = findViewById(R.id.btnNavHome)
        btnBox = findViewById(R.id.btnNavBox)
        btnAccess = findViewById(R.id.btnNavAccess)
        btnEmergency = findViewById(R.id.btnNavEmergency)

        setupDefaultNavigation()
    }

    private fun setupDefaultNavigation() {
        btnHome.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(intent)
        }

        btnBox.setOnClickListener {
            val intent = Intent(context, ObjetosActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(intent)
        }

        btnAccess.setOnClickListener {
            val intent = Intent(context, AccessMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(intent)
        }

        btnEmergency.setOnClickListener {
            val intent = Intent(context, ReportarEmergenciaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(intent)
        }
    }

    // Keep this method for backwards compatibility or overrides
    fun setOnButtonsClickListener(
        onHome: () -> Unit,
        onBox: () -> Unit,
        onAccess: () -> Unit,
        onEmergency: () -> Unit
    ) {
        btnHome.setOnClickListener { onHome() }
        btnBox.setOnClickListener { onBox() }
        btnAccess.setOnClickListener { onAccess() }
        btnEmergency.setOnClickListener { onEmergency() }
    }
}