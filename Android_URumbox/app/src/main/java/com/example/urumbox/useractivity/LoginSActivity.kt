package com.example.urumbox.useractivity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.urumbox.MainActivity
import com.example.urumbox.data.AuthResult
import com.example.urumbox.databinding.ActivityLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
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

        binding.tvOlvidasteTuContrasena.setOnClickListener {
            mostrarDialogoRecuperacion()
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

    private fun mostrarDialogoRecuperacion() {
        // IMPORTANTE: Para que el envío funcione, el correo DEBE existir registrado en
        // Firebase Console → Authentication → Users. Firebase no puede enviar el enlace
        // a correos que no estén registrados en Firebase Authentication.
        val inputLayout = TextInputLayout(this)
        val inputField = TextInputEditText(this).apply {
            hint = "nombre@urosario.edu.co"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        inputLayout.addView(inputField)

        val padding = (20 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(this).apply {
            setPadding(padding, padding / 4, padding, 0)
        }
        container.addView(inputLayout)

        // setPositiveButton(null) evita que el diálogo se cierre automáticamente al pulsar Enviar
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Recuperar contraseña")
            .setMessage("Ingresa tu correo institucional @urosario.edu.co")
            .setView(container)
            .setPositiveButton("Enviar", null)
            .setNegativeButton("Cancelar", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val correo = inputField.text.toString().trim()
            if (correo.isEmpty() || !correo.endsWith("@urosario.edu.co")) {
                Toast.makeText(this, "Ingresa un correo válido @urosario.edu.co", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(correo)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Se envió un enlace de restablecimiento a tu correo institucional. Revisa tu bandeja de entrada y spam.",
                        Toast.LENGTH_LONG
                    ).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun mostrarError(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error al iniciar sesión")
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }
}
