package com.example.urumbox

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.urumbox.databinding.ActivityAccessMainBinding
import com.google.android.material.button.MaterialButton

class AccessMainActivity : AppCompatActivity() {

    private val viewModel: AccessMainViewModel by viewModels()
    private lateinit var binding: ActivityAccessMainBinding
    private var addVisitorDialog: Dialog? = null

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
            viewModel.onAddVisitorClicked()
        }

        binding.btnQrCode.setOnClickListener {
            startActivity(Intent(this, AccessQrActivity::class.java))
        }

        viewModel.uiEvent.observe(this) { event ->
            when (event) {
                is AccessMainEvent.ShowAddVisitorDialog -> {
                    showAddVisitorDialog()
                    viewModel.onEventConsumed()
                }
                is AccessMainEvent.NavigateToRequest -> {
                    addVisitorDialog?.dismiss()
                    startActivity(Intent(this, AccessRequestActivity::class.java))
                    viewModel.onEventConsumed()
                }
                is AccessMainEvent.NavigateToConsult -> {
                    addVisitorDialog?.dismiss()
                    startActivity(Intent(this, AccessRequestConsultActivity::class.java))
                    viewModel.onEventConsumed()
                }
                null -> Unit
            }
        }
    }

    private fun showAddVisitorDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_visitor_options)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(true)

        dialog.findViewById<MaterialButton>(R.id.btnRegistrarSolicitud).setOnClickListener {
            viewModel.onRegisterRequestClicked()
        }
        dialog.findViewById<MaterialButton>(R.id.btnConsultarSolicitudes).setOnClickListener {
            viewModel.onConsultRequestsClicked()
        }

        addVisitorDialog = dialog
        dialog.show()
    }
}
