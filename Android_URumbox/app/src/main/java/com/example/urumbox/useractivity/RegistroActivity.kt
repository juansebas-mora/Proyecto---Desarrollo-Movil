package com.example.urumbox.useractivity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.urumbox.data.AuthResult
import com.example.urumbox.databinding.ActivityRegistroBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding
    private val viewModel: RegistroViewModel by lazy {
        ViewModelProvider(this)[RegistroViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistrarse.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val apellido = binding.etApellido.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val fecha = binding.etFecha.text.toString().trim()
            val correo = binding.etCorreoReg.text.toString().trim()
            val contrasena = binding.etContrasenaReg.text.toString().trim()
            if (validarCampos(nombre, apellido, telefono, fecha, correo, contrasena)) {
                viewModel.registrar(correo, contrasena, nombre, apellido, telefono, fecha)
            }
        }

        binding.tvIniciarSesion.setOnClickListener {
            startActivity(Intent(this, LoginSActivity::class.java))
            finish()
        }

        lifecycleScope.launch {
            viewModel.registroState.collect { state ->
                when (state) {
                    is AuthResult.Loading -> setLoading(true)
                    is AuthResult.Success -> {
                        setLoading(false)
                        MaterialAlertDialogBuilder(this@RegistroActivity)
                            .setTitle("Registro exitoso")
                            .setMessage("Tu cuenta ha sido creada correctamente. Ya puedes iniciar sesión.")
                            .setPositiveButton("Iniciar sesión") { _, _ ->
                                startActivity(Intent(this@RegistroActivity, LoginSActivity::class.java))
                                finish()
                            }
                            .setCancelable(false)
                            .show()
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

    private fun validarCampos(
        nombre: String, apellido: String, telefono: String,
        fecha: String, correo: String, contrasena: String
    ): Boolean {
        var valid = true
        if (nombre.isEmpty()) { binding.inputLayoutNombre.error = "Campo requerido"; valid = false } else binding.inputLayoutNombre.error = null
        if (apellido.isEmpty()) { binding.inputLayoutApellido.error = "Campo requerido"; valid = false } else binding.inputLayoutApellido.error = null
        if (telefono.isEmpty()) { binding.inputLayoutTelefono.error = "Campo requerido"; valid = false } else binding.inputLayoutTelefono.error = null
        if (fecha.isEmpty()) { binding.inputLayoutFecha.error = "Campo requerido"; valid = false } else binding.inputLayoutFecha.error = null
        if (correo.isEmpty()) {
            binding.inputLayoutCorreoReg.error = "Campo requerido"; valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.inputLayoutCorreoReg.error = "Correo electrónico inválido"; valid = false
        } else {
            binding.inputLayoutCorreoReg.error = null
        }
        if (contrasena.isEmpty()) {
            binding.inputLayoutContrasenaReg.error = "Campo requerido"; valid = false
        } else if (contrasena.length < 6) {
            binding.inputLayoutContrasenaReg.error = "Mínimo 6 caracteres"; valid = false
        } else {
            binding.inputLayoutContrasenaReg.error = null
        }
        return valid
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegistrarse.isEnabled = !loading
    }

    private fun mostrarError(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error al registrarse")
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }
}
