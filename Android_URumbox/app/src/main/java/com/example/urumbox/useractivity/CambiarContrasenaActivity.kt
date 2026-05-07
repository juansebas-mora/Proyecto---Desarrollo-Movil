package com.example.urumbox.useractivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.urumbox.data.AuthResult
import com.example.urumbox.databinding.ActivityCambiarContrasenaBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class CambiarContrasenaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCambiarContrasenaBinding
    private val viewModel: CambiarContrasenaViewModel by lazy {
        ViewModelProvider(this)[CambiarContrasenaViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCambiarContrasenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnGuardar.setOnClickListener {
            val actual = binding.etContrasenaActual.text.toString().trim()
            val nueva = binding.etContrasenaNueva.text.toString().trim()
            val confirmar = binding.etContrasenaConfirmar.text.toString().trim()
            if (validarCampos(actual, nueva, confirmar)) {
                viewModel.cambiarContrasena(actual, nueva)
            }
        }

        lifecycleScope.launch {
            viewModel.cambioState.collect { state ->
                when (state) {
                    is AuthResult.Loading -> binding.btnGuardar.isEnabled = false
                    is AuthResult.Success -> {
                        binding.btnGuardar.isEnabled = true
                        MaterialAlertDialogBuilder(this@CambiarContrasenaActivity)
                            .setTitle("Contraseña actualizada")
                            .setMessage("Tu contraseña ha sido cambiada correctamente.")
                            .setPositiveButton("Aceptar") { _, _ -> finish() }
                            .setCancelable(false)
                            .show()
                    }
                    is AuthResult.Error -> {
                        binding.btnGuardar.isEnabled = true
                        MaterialAlertDialogBuilder(this@CambiarContrasenaActivity)
                            .setTitle("Error")
                            .setMessage(state.message)
                            .setPositiveButton("Aceptar", null)
                            .show()
                    }
                    null -> Unit
                }
            }
        }
    }

    private fun validarCampos(actual: String, nueva: String, confirmar: String): Boolean {
        var valid = true
        if (actual.isEmpty()) { binding.inputLayoutContrasenaActual.error = "Campo requerido"; valid = false } else binding.inputLayoutContrasenaActual.error = null
        if (nueva.isEmpty()) {
            binding.inputLayoutContrasenaNueva.error = "Campo requerido"; valid = false
        } else if (nueva.length < 6) {
            binding.inputLayoutContrasenaNueva.error = "Mínimo 6 caracteres"; valid = false
        } else {
            binding.inputLayoutContrasenaNueva.error = null
        }
        if (confirmar.isEmpty()) { binding.inputLayoutContrasenaConfirmar.error = "Campo requerido"; valid = false } else binding.inputLayoutContrasenaConfirmar.error = null
        if (valid && nueva != confirmar) {
            binding.inputLayoutContrasenaConfirmar.error = "Las contraseñas no coinciden"
            valid = false
        }
        return valid
    }
}
