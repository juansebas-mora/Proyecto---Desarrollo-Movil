package com.example.urumbox.emergencyactivity

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import com.example.urumbox.R

class DialogReporteExitoso(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion_estado)

        // Fondo transparente para que se vea el rounded corners
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCerrar = findViewById<Button>(R.id.btnCerrarDialog)
        btnCerrar.setOnClickListener {
            dismiss() // cierra el dialog
        }
    }
}