package com.example.urumbox.accessactivity

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urumbox.R
import com.example.urumbox.data.model.AccessRequest
import com.example.urumbox.data.repository.AccessRequestRepository
import com.example.urumbox.databinding.ActivityAdminSolicitudesBinding
import com.google.android.material.button.MaterialButton

class AdminSolicitudesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSolicitudesBinding
    private lateinit var adapter: AdminSolicitudesAdapter
    private val repository = AccessRequestRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityAdminSolicitudesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.topBar.setOnBackClickListener { finish() }

        adapter = AdminSolicitudesAdapter(
            mutableListOf(),
            onAceptar = { solicitud -> confirmarCambioEstado(solicitud, "aceptada") },
            onDenegar = { solicitud -> confirmarCambioEstado(solicitud, "denegada") }
        )
        binding.recyclerSolicitudes.layoutManager = LinearLayoutManager(this)
        binding.recyclerSolicitudes.adapter = adapter

        cargarSolicitudes()
    }

    private fun cargarSolicitudes() {
        repository.getAllAccessRequests { result ->
            result.onSuccess { solicitudes ->
                adapter.updateItems(solicitudes)
                binding.tvSolicitudesEmpty.visibility =
                    if (solicitudes.isEmpty()) View.VISIBLE else View.GONE
            }
            result.onFailure {
                Toast.makeText(this, "Error al cargar solicitudes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarCambioEstado(solicitud: AccessRequest, nuevoEstado: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_confirmar_solicitud)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(true)

        val titulo = if (nuevoEstado == "aceptada") "Aceptar solicitud" else "Denegar solicitud"
        val mensaje = if (nuevoEstado == "aceptada")
            "¿Deseas aceptar la solicitud de acceso para ${solicitud.nombres} ${solicitud.apellidos}?"
        else
            "¿Deseas denegar la solicitud de acceso para ${solicitud.nombres} ${solicitud.apellidos}?"

        dialog.findViewById<TextView>(R.id.tvTituloConfirmar).text = titulo
        dialog.findViewById<TextView>(R.id.tvMensajeConfirmar).text = mensaje

        val btnConfirmar = dialog.findViewById<MaterialButton>(R.id.btnConfirmarSolicitud)
        val colorRes = if (nuevoEstado == "aceptada") R.color.text_bajo else R.color.rojo_ur
        btnConfirmar.backgroundTintList = ContextCompat.getColorStateList(this, colorRes)
        btnConfirmar.text = if (nuevoEstado == "aceptada") "Aceptar" else "Denegar"

        btnConfirmar.setOnClickListener {
            dialog.dismiss()
            repository.updateAccessRequestStatus(solicitud.id, nuevoEstado) { result ->
                result.onSuccess {
                    val msg = if (nuevoEstado == "aceptada") "Solicitud aceptada" else "Solicitud denegada"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    cargarSolicitudes()
                }
                result.onFailure {
                    Toast.makeText(this, "Error al actualizar la solicitud", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.findViewById<MaterialButton>(R.id.btnCancelarSolicitud).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
