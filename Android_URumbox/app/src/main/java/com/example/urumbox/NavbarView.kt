package com.example.urumbox

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt

class NavbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    // Botones
    private val btnHome: ImageButton
    private val btnBox: ImageButton
    private val btnAccess: ImageButton
    private val btnEmergency: ImageButton

    init {
        // Inflar component_navbar.xml
        LayoutInflater.from(context).inflate(R.layout.navbar_content, this, true)

        setBackgroundColor(ContextCompat.getColor(context, R.color.barra))

        // Inicializamos los botones usando los IDs de tu archivo XML
        btnHome = findViewById(R.id.btnNavHome)
        btnBox = findViewById(R.id.btnNavBox)
        btnAccess = findViewById(R.id.btnNavAccess)
        btnEmergency = findViewById(R.id.btnNavEmergency)
    }

    /**
     * Método para asignar funciones a los clics desde cualquier Activity
     */
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

    /**
    fun setOnHomeClickListener(action: () -> Unit) = btnHome.setOnClickListener { action() }
    fun setOnBoxClickListener(action: () -> Unit) = btnBox.setOnClickListener { action() }
    fun setOnAccessClickListener(action: () -> Unit) = btnAccess.setOnClickListener { action() }
    fun setOnEmergencyClickListener(action: () -> Unit) = btnEmergency.setOnClickListener { action() }
     */
}