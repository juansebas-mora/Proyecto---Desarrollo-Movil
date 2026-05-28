package com.example.urumbox.useractivity

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.urumbox.MainActivity
import com.example.urumbox.R
import com.example.urumbox.vigilante.VigilanteMainActivity
import com.example.urumbox.data.AuthResult
import com.example.urumbox.databinding.ActivityCambiarContrasenaBinding
import com.example.urumbox.databinding.ActivityComentarioBinding
import com.example.urumbox.databinding.ActivityConfiguracionBinding
import com.example.urumbox.databinding.ActivityContactanosBinding
import com.example.urumbox.databinding.ActivityInfoPersonalBinding
import com.example.urumbox.databinding.ActivityLoginBinding
import com.example.urumbox.databinding.ActivityPerfilBinding
import com.example.urumbox.databinding.ActivityPermisosBinding
import com.example.urumbox.databinding.ActivityRegistroBinding
import com.example.urumbox.databinding.ActivityVersionBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.Calendar
import kotlinx.coroutines.launch

// region CambiarContrasenaActivity

class CambiarContrasenaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCambiarContrasenaBinding
    private val viewModel: CambiarContrasenaViewModel by lazy {
        ViewModelProvider(this)[CambiarContrasenaViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityCambiarContrasenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.topBar.setOnBackClickListener { finish() }

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
                        mostrarDialogoContrasenaActualizada()
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

    private fun mostrarDialogoContrasenaActualizada() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_contrasena_actualizada)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(false)
        dialog.findViewById<MaterialButton>(R.id.btnAceptar).setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.show()
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

// endregion

// region ComentarioActivity

class ComentarioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityComentarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityComentarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.topBar.setOnBackClickListener { finish() }

        binding.btnEnviar.setOnClickListener {
            val asunto = binding.etAsunto.text.toString().trim()
            val comentario = binding.etComentario.text.toString().trim()

            if (asunto.isEmpty()) {
                Toast.makeText(this, "El asunto no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (comentario.isEmpty()) {
                Toast.makeText(this, "El comentario no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val correo = FirebaseAuth.getInstance().currentUser?.email ?: ""

            val nuevoComentario = hashMapOf(
                "uid" to uid,
                "correo" to correo,
                "asunto" to asunto,
                "comentario" to comentario,
                "fecha" to Timestamp.now(),
                "estado" to "pendiente"
            )

            FirebaseFirestore.getInstance().collection("comentarios")
                .add(nuevoComentario)
                .addOnSuccessListener {
                    Toast.makeText(this, "Comentario enviado correctamente", Toast.LENGTH_SHORT).show()
                    binding.etAsunto.setText("")
                    binding.etComentario.setText("")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al enviar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

// endregion

// region ConfiguracionActivity

class ConfiguracionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfiguracionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.topBar.setOnBackClickListener { finish() }

        binding.itemPermisos.setOnClickListener {
            startActivity(Intent(this, PermisosActivity::class.java))
        }

        binding.itemCambiarContrasena.setOnClickListener {
            startActivity(Intent(this, CambiarContrasenaActivity::class.java))
        }

        binding.itemComentario.setOnClickListener {
            startActivity(Intent(this, ComentarioActivity::class.java))
        }

        binding.itemContactanos.setOnClickListener {
            startActivity(Intent(this, ContactanosActivity::class.java))
        }

        binding.itemVersion.setOnClickListener {
            startActivity(Intent(this, VersionActivity::class.java))
        }
    }
}

// endregion

// region ContactanosActivity

class ContactanosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactanosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityContactanosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.topBar.setOnBackClickListener { finish() }
    }
}

// endregion

// region InfoPersonalActivity

class InfoPersonalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoPersonalBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityInfoPersonalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadUserData()
        setupPhoneEdit()
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        db.collection("usuarios").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val nombreCompleto = doc.getString("nombreCompleto")
                    ?: "${doc.getString("nombre") ?: ""} ${doc.getString("apellido") ?: ""}".trim()
                val correo = doc.getString("correo")
                    ?: doc.getString("email")
                    ?: user.email
                    ?: ""
                val usuario = doc.getString("usuario")
                    ?: correo.substringBefore("@")
                val rol = doc.getString("rol") ?: ""

                binding.tvNombreValor.text = nombreCompleto
                binding.tvUsuarioValor.text = usuario
                binding.tvCorreoValor.text = correo
                binding.tvRolValor.text = rol
                binding.tvDocumentoValor.text = doc.getString("documentoIdentidad") ?: ""
                binding.etTelefono.setText(doc.getString("telefono") ?: "")
                binding.tvNacimientoValor.text = doc.getString("fechaNacimiento") ?: ""
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar información personal", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupPhoneEdit() {
        binding.ivEditTelefono.setOnClickListener {
            binding.etTelefono.isEnabled = true
            binding.etTelefono.requestFocus()
            binding.ivEditTelefono.visibility = View.GONE
            binding.ivGuardarTelefono.visibility = View.VISIBLE
        }

        binding.ivGuardarTelefono.setOnClickListener {
            val newPhone = binding.etTelefono.text.toString().trim()
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("usuarios").document(uid)
                .update("telefono", newPhone)
                .addOnSuccessListener {
                    binding.etTelefono.isEnabled = false
                    binding.ivEditTelefono.visibility = View.VISIBLE
                    binding.ivGuardarTelefono.visibility = View.GONE
                    Toast.makeText(this, "Teléfono actualizado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar el teléfono", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

// endregion

// region PerfilActivity

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var cameraImageUri: Uri? = null
    private var userRol: String = ""

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { uploadProfilePhoto(it) } }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraImageUri?.let { uploadProfilePhoto(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.topBar.setOnBackClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        loadUserData()

        binding.imgAvatar.setOnClickListener { showPhotoSourceDialog() }

        binding.itemInfoPersonal.setOnClickListener {
            startActivity(Intent(this, InfoPersonalActivity::class.java))
        }

        binding.itemConfiguracion.setOnClickListener {
            startActivity(Intent(this, ConfiguracionActivity::class.java))
        }

        binding.itemUsuarios.setOnClickListener {
            startActivity(Intent(this, GestionUsuariosActivity::class.java))
        }

        binding.itemVerComentarios.setOnClickListener {
            startActivity(Intent(this, VerComentariosActivity::class.java))
        }

        binding.itemValidarQR.setOnClickListener {
            Toast.makeText(this, "Módulo de validación QR próximamente disponible.", Toast.LENGTH_SHORT).show()
        }

        binding.itemCerrarSesion.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, InicioActivity::class.java))
            finishAffinity()
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (userRol == "Vigilante") {
            navigateToVigilanteMain()
        } else {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToVigilanteMain() {
        val intent = Intent(this, VigilanteMainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        db.collection("usuarios").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val nombreCompleto = doc.getString("nombreCompleto")
                    ?: "${doc.getString("nombre") ?: ""} ${doc.getString("apellido") ?: ""}".trim()
                binding.tvNombre.text = nombreCompleto.ifEmpty { user.email ?: "" }
                val rol = doc.getString("rol") ?: ""
                userRol = rol
                binding.tvRol.text = rol
                val fotoUrl = doc.getString("fotoPerfil")
                if (!fotoUrl.isNullOrEmpty()) {
                    Glide.with(this).load(fotoUrl).circleCrop().into(binding.imgAvatar)
                }
                // Aplicar visibilidad de ítems del menú según el rol
                aplicarPermisosPorRol(rol)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun aplicarPermisosPorRol(rol: String) {
        when (rol) {
            "Administrador" -> {
                binding.itemUsuarios.visibility = View.VISIBLE
                binding.dividerUsuarios.visibility = View.VISIBLE
                binding.itemVerComentarios.visibility = View.VISIBLE
                binding.dividerVerComentarios.visibility = View.VISIBLE
                binding.itemValidarQR.visibility = View.GONE
                binding.dividerValidarQR.visibility = View.GONE
            }
            "Vigilante" -> {
                binding.itemInfoPersonal.visibility = View.GONE
                binding.dividerInfoPersonal.visibility = View.GONE
                binding.itemConfiguracion.visibility = View.GONE
                binding.dividerConfiguracion.visibility = View.GONE
                binding.itemUsuarios.visibility = View.GONE
                binding.dividerUsuarios.visibility = View.GONE
                binding.itemValidarQR.visibility = View.VISIBLE
                binding.dividerValidarQR.visibility = View.VISIBLE

                binding.topBar.setNotificationButtonVisible(false)
                binding.navbar.visibility = View.GONE

                binding.itemValidarQR.setOnClickListener {
                    navigateToVigilanteMain()
                }

                binding.navbar.setOnButtonsClickListener(
                    onHome = {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                    },
                    onBox = {
                        val intent = Intent(this, com.example.urumbox.objetosactivity.ObjetosActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                    },
                    onAccess = { navigateToVigilanteMain() },
                    onEmergency = {
                        val intent = Intent(this, com.example.urumbox.emergencyactivity.EmergenciasActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                    }
                )
            }
            else -> {
                // "Usuario UR" o rol vacío: sin acceso a gestión de usuarios ni validación QR
                binding.itemUsuarios.visibility = View.GONE
                binding.dividerUsuarios.visibility = View.GONE
                binding.itemValidarQR.visibility = View.GONE
                binding.dividerValidarQR.visibility = View.GONE
            }
        }
    }

    private fun showPhotoSourceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Foto de perfil")
            .setItems(arrayOf("Tomar foto", "Elegir de galería")) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun openCamera() {
        val photoFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "profile_${System.currentTimeMillis()}.jpg"
        )
        cameraImageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        cameraImageUri?.let { uri ->
            cameraLauncher.launch(uri)
        }
    }

    private fun uploadProfilePhoto(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("fotos_perfil/$uid.jpg")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri: Uri ->
                    db.collection("usuarios").document(uid)
                        .update("fotoPerfil", downloadUri.toString())
                        .addOnSuccessListener {
                            Glide.with(this).load(downloadUri).circleCrop().into(binding.imgAvatar)
                            Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar URL de foto", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir the foto", Toast.LENGTH_SHORT).show()
            }
    }
}

// endregion

// region PermisosActivity

@Suppress("DEPRECATION")
class PermisosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermisosBinding

    private var isUpdatingSwitch = false

    companion object {
        private const val REQ_UBICACION = 100
        private const val REQ_CAMARA = 101
        private const val REQ_NOTIFICACIONES = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityPermisosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.topBar.setOnBackClickListener { finish() }

        updateSwitchStates()
        setupSwitchListeners()
    }

    override fun onResume() {
        super.onResume()
        updateSwitchStates()
    }

    private fun updateSwitchStates() {
        isUpdatingSwitch = true
        binding.switchUbicacion.isChecked = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        binding.switchCamara.isChecked = hasPermission(Manifest.permission.CAMERA)
        binding.switchNotificaciones.isChecked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else true
        isUpdatingSwitch = false
    }

    private fun hasPermission(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun setupSwitchListeners() {
        binding.switchUbicacion.setOnCheckedChangeListener { _, isChecked ->
            onSwitchChanged(binding.switchUbicacion, isChecked, Manifest.permission.ACCESS_FINE_LOCATION, REQ_UBICACION)
        }
        binding.switchCamara.setOnCheckedChangeListener { _, isChecked ->
            onSwitchChanged(binding.switchCamara, isChecked, Manifest.permission.CAMERA, REQ_CAMARA)
        }
        binding.switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                onSwitchChanged(binding.switchNotificaciones, isChecked, Manifest.permission.POST_NOTIFICATIONS, REQ_NOTIFICACIONES)
            } else if (!isChecked && !isUpdatingSwitch) {
                isUpdatingSwitch = true
                binding.switchNotificaciones.isChecked = true
                isUpdatingSwitch = false
                Toast.makeText(this, "Para desactivar notificaciones, ve a Configuración de la app", Toast.LENGTH_LONG).show()
                openAppSettings()
            }
        }
    }

    private fun onSwitchChanged(switch: Switch, isChecked: Boolean, permission: String, requestCode: Int) {
        if (isUpdatingSwitch) return
        if (isChecked) {
            if (!hasPermission(permission)) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        } else {
            if (hasPermission(permission)) {
                isUpdatingSwitch = true
                switch.isChecked = true
                isUpdatingSwitch = false
                Toast.makeText(this, "Para desactivar el permiso, ve a Configuración de la app", Toast.LENGTH_LONG).show()
                openAppSettings()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        isUpdatingSwitch = true
        when (requestCode) {
            REQ_UBICACION -> binding.switchUbicacion.isChecked = granted
            REQ_CAMARA -> binding.switchCamara.isChecked = granted
            REQ_NOTIFICACIONES -> binding.switchNotificaciones.isChecked = granted
        }
        isUpdatingSwitch = false
    }

    private fun openAppSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        })
    }
}

// endregion

// region RegistroActivity

class RegistroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding
    private val viewModel: RegistroViewModel by lazy {
        ViewModelProvider(this)[RegistroViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val hintColors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
            intArrayOf(
                ContextCompat.getColor(this, R.color.secundario),
                ContextCompat.getColor(this, R.color.azul_ur)
            )
        )
        binding.inputLayoutNombre.defaultHintTextColor = hintColors
        binding.inputLayoutApellido.defaultHintTextColor = hintColors
        binding.inputLayoutTelefono.defaultHintTextColor = hintColors
        binding.inputLayoutDocumento.defaultHintTextColor = hintColors
        binding.inputLayoutFecha.defaultHintTextColor = hintColors
        binding.inputLayoutCorreoReg.defaultHintTextColor = hintColors
        binding.inputLayoutContrasenaReg.defaultHintTextColor = hintColors

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
            val documento = binding.etDocumento.text.toString().trim()

            if (!validarCampos(nombre, apellido, telefono, fecha, correo, contrasena)) return@setOnClickListener

            if (documento.isEmpty()) {
                Toast.makeText(this, "El número de documento es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!correo.endsWith("@urosario.edu.co")) {
                Toast.makeText(
                    this,
                    "Solo puedes registrarte con un correo institucional @urosario.edu.co",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(correo)
                .addOnSuccessListener { result ->
                    if (!result.signInMethods.isNullOrEmpty()) {
                        Toast.makeText(
                            this,
                            "Este correo ya está registrado. Inicia sesión o recupera tu contraseña.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        viewModel.registrar(correo, contrasena, nombre, apellido, telefono, fecha, documento)
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
                        mostrarDialogoRegistroExitoso()
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

    private fun mostrarDialogoRegistroExitoso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_registro_exitoso)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(false)
        dialog.findViewById<MaterialButton>(R.id.btnIniciarSesion).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, LoginSActivity::class.java))
            finish()
        }
        dialog.show()
    }

    private fun mostrarSelectorFecha() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
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

// endregion

// region VersionActivity

class VersionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVersionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityVersionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }
}

// endregion

// region InicioActivity

class InicioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("usuarios").document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    val rol = doc.getString("rol") ?: ""
                    if (rol == "Vigilante") {
                        startActivity(Intent(this, VigilanteMainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            return
        }

        setContentView(R.layout.activity_inicio)

        findViewById<Button>(R.id.btnInicioSesion).setOnClickListener {
            startActivity(Intent(this, LoginSActivity::class.java))
        }

        findViewById<Button>(R.id.btnRegistrarse).setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }
}

// endregion

// region LoginSActivity

class LoginSActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val hintColors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
            intArrayOf(
                ContextCompat.getColor(this, R.color.secundario),
                ContextCompat.getColor(this, R.color.azul_ur)
            )
        )
        binding.inputLayoutCorreo.defaultHintTextColor = hintColors
        binding.inputLayoutContrasena.defaultHintTextColor = hintColors

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
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@collect
                        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
                            .addOnSuccessListener { doc ->
                                if (doc.getString("estado") == "inactivo") {
                                    FirebaseAuth.getInstance().signOut()
                                    Toast.makeText(
                                        this@LoginSActivity,
                                        "Tu cuenta ha sido desactivada. Contacta al administrador.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    viewModel.resolveDestination(uid)
                                }
                            }
                            .addOnFailureListener {
                                viewModel.resolveDestination(uid)
                            }
                    }
                    is AuthResult.Error -> {
                        setLoading(false)
                        mostrarError(state.message)
                    }
                    null -> Unit
                }
            }
        }

        lifecycleScope.launch {
            viewModel.destination.collect { dest ->
                dest ?: return@collect
                when (dest) {
                    is LoginDestination.Vigilante -> {
                        startActivity(Intent(this@LoginSActivity, VigilanteMainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                    is LoginDestination.Main -> {
                        startActivity(Intent(this@LoginSActivity, MainActivity::class.java))
                        finish()
                    }
                }
                viewModel.onDestinationConsumed()
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
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_recuperar_contrasena)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(true)

        val correoUsuario = binding.etCorreo.text.toString().trim()
        dialog.findViewById<android.widget.TextView>(R.id.tvCorreoSoporte).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:urumbox@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Recuperación de contraseña - $correoUsuario")
            }
            startActivity(intent)
        }
        dialog.findViewById<MaterialButton>(R.id.btnEntendido).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun mostrarError(mensaje: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_error_login)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(true)
        dialog.findViewById<android.widget.TextView>(R.id.tvMensajeError).text = mensaje
        dialog.findViewById<MaterialButton>(R.id.btnIntentarDeNuevo).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}

// endregion
