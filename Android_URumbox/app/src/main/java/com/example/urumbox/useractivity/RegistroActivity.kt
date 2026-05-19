package com.example.urumbox.useractivity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.urumbox.data.AuthResult
import com.example.urumbox.databinding.ActivityRegistroBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.launch
import java.util.Calendar

class RegistroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding
    private val viewModel: RegistroViewModel by lazy {
        ViewModelProvider(this)[RegistroViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // Campo fecha: solo lectura, abre DatePickerDialog al tocar (sin teclado)
        binding.etFecha.apply {
            isFocusable = false
            isClickable = true
            isCursorVisible = false
        }
        binding.etFecha.setOnClickListener { mostrarSelectorFecha() }
        binding.inputLayoutFecha.setOnClickListener { mostrarSelectorFecha() }

        binding.btnRegistrarse.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val apellido = binding.etApellido.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val fecha = binding.etFecha.text.toString().trim()
            val correo = binding.etCorreoReg.text.toString().trim()
            val contrasena = binding.etContrasenaReg.text.toString().trim()

            if (!validarCampos(nombre, apellido, telefono, fecha, correo, contrasena)) return@setOnClickListener

            // Capa 1: validar dominio institucional
            if (!correo.endsWith("@urosario.edu.co")) {
                Toast.makeText(
                    this,
                    "Solo puedes registrarte con un correo institucional @urosario.edu.co",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Capa 2: verificar si el correo ya está registrado en Firebase Auth.
            // NOTA: Firebase NO puede verificar directamente si un correo existe en Outlook/Microsoft.
            // Solo verifica si el correo ya está registrado en Firebase Authentication.
            // La validación del dominio @urosario.edu.co es la barrera principal para
            // restringir el registro a usuarios institucionales.
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(correo)
                .addOnSuccessListener { result ->
                    if (!result.signInMethods.isNullOrEmpty()) {
                        Toast.makeText(
                            this,
                            "Este correo ya está registrado. Inicia sesión o recupera tu contraseña.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        viewModel.registrar(correo, contrasena, nombre, apellido, telefono, fecha)
                    }
                }
                .addOnFailureListener { e ->
                    when (e) {
                        is FirebaseAuthInvalidCredentialsException ->
                            Toast.makeText(this, "El formato del correo no es válido.", Toast.LENGTH_SHORT).show()
                        else ->
                            Toast.makeText(this, "No se pudo verificar el correo. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                    }
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

    private fun mostrarSelectorFecha() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                // Formato DD/MM/AAAA
                binding.etFecha.setText(String.format("%02d/%02d/%04d", day, month + 1, year))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
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
