package com.example.urumbox.useractivity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.urumbox.MainActivity
import com.example.urumbox.data.AuthResult
import com.example.urumbox.databinding.ActivityLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class LoginSActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIngresar.setOnClickListener {
            val correo = binding.etCorreo.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()
            if (validarCampos(correo, contrasena)) {
                viewModel.login(correo, contrasena)
            }
        }

        binding.tvRegistrate.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is AuthResult.Loading -> setLoading(true)
                    is AuthResult.Success -> {
                        setLoading(false)
                        startActivity(Intent(this@LoginSActivity, MainActivity::class.java))
                        finish()
                    }
                    is AuthResult.Error -> {
                        setLoading(false)
                        mostrarError(state.message)
                    }
                    null -> Unit
                }
            }
        }
    }

    private fun validarCampos(correo: String, contrasena: String): Boolean {
        var valid = true
        if (correo.isEmpty()) {
            binding.inputLayoutCorreo.error = "Campo requerido"
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.inputLayoutCorreo.error = "Correo electrónico inválido"
            valid = false
        } else {
            binding.inputLayoutCorreo.error = null
        }
        if (contrasena.isEmpty()) {
            binding.inputLayoutContrasena.error = "Campo requerido"
            valid = false
        } else {
            binding.inputLayoutContrasena.error = null
        }
        return valid
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnIngresar.isEnabled = !loading
    }

    private fun mostrarError(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error al iniciar sesión")
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }
}
