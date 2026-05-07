package com.example.urumbox.accessactivity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urumbox.R
import com.example.urumbox.data.model.AccessRequest
import com.example.urumbox.databinding.ActivityAccessHistoryBinding
import com.example.urumbox.databinding.ActivityAccessMainBinding
import com.example.urumbox.databinding.ActivityAccessQrBinding
import com.example.urumbox.databinding.ActivityAccessRequestBinding
import com.example.urumbox.databinding.ActivityAccessRequestConsultBinding
import com.google.android.material.button.MaterialButton

// region AccessMainActivity

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

        viewModel.loadUserInfo()
        viewModel.userName.observe(this) { name -> binding.tvUserName.text = name }
        viewModel.userEmail.observe(this) { email -> binding.tvUserEmail.text = email }

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

// endregion

// region AccessHistoryActivity

class AccessHistoryActivity : AppCompatActivity() {

    private val viewModel: AccessHistoryViewModel by viewModels()
    private lateinit var binding: ActivityAccessHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityAccessHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

// endregion

// region AccessQrActivity

class AccessQrActivity : AppCompatActivity() {

    private val viewModel: AccessQrViewModel by viewModels()
    private lateinit var binding: ActivityAccessQrBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityAccessQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

// endregion

// region AccessRequestActivity

class AccessRequestActivity : AppCompatActivity() {

    private val viewModel: AccessRequestViewModel by viewModels()
    private lateinit var binding: ActivityAccessRequestBinding
    private var confirmationDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityAccessRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupTextWatchers()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupTextWatchers() {
        binding.etNombres.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                viewModel.validateNombres(s.toString())
            }
        })

        binding.etApellidos.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                viewModel.validateApellidos(s.toString())
            }
        })

        binding.etCorreo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                viewModel.validateCorreo(s.toString())
            }
        })

        binding.etDocumento.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                viewModel.validateDocumento(s.toString())
            }
        })

        binding.etFecha.addTextChangedListener(DateMaskWatcher())
        binding.etFecha.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                viewModel.validateFecha(s.toString())
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnRegistrarSolicitud.setOnClickListener {
            viewModel.onRegisterClicked(
                nombres = binding.etNombres.text.toString(),
                apellidos = binding.etApellidos.text.toString(),
                correo = binding.etCorreo.text.toString(),
                documento = binding.etDocumento.text.toString(),
                fecha = binding.etFecha.text.toString()
            )
        }
    }

    private fun observeViewModel() {
        viewModel.nombresError.observe(this) { error ->
            showFieldError(binding.tvErrorNombres, error)
        }
        viewModel.apellidosError.observe(this) { error ->
            showFieldError(binding.tvErrorApellidos, error)
        }
        viewModel.correoError.observe(this) { error ->
            showFieldError(binding.tvErrorCorreo, error)
        }
        viewModel.documentoError.observe(this) { error ->
            showFieldError(binding.tvErrorDocumento, error)
        }
        viewModel.fechaError.observe(this) { error ->
            showFieldError(binding.tvErrorFecha, error)
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.btnRegistrarSolicitud.isEnabled = !loading
        }

        viewModel.registrationResult.observe(this) { result ->
            result ?: return@observe
            if (result.isSuccess) {
                showConfirmationDialog()
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Error al registrar la solicitud"
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
            viewModel.onRegistrationResultConsumed()
        }

        viewModel.uiEvent.observe(this) { event ->
            when (event) {
                is AccessRequestEvent.NavigateToConsult -> {
                    confirmationDialog?.dismiss()
                    startActivity(Intent(this, AccessRequestConsultActivity::class.java))
                    viewModel.onEventConsumed()
                }
                null -> Unit
            }
        }
    }

    private fun showFieldError(view: TextView, error: String?) {
        if (error != null) {
            view.text = error
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    private fun showConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_solicitud_registrada)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(false)

        dialog.findViewById<MaterialButton>(R.id.btnAceptar).setOnClickListener {
            dialog.dismiss()
            clearForm()
        }
        dialog.findViewById<MaterialButton>(R.id.btnConsultarSolicitudes).setOnClickListener {
            viewModel.onNavigateToConsult()
        }

        confirmationDialog = dialog
        dialog.show()
    }

    private fun clearForm() {
        binding.etNombres.setText("")
        binding.etApellidos.setText("")
        binding.etCorreo.setText("")
        binding.etDocumento.setText("")
        binding.etFecha.setText("")
        viewModel.clearErrors()
    }

    private inner class DateMaskWatcher : TextWatcher {
        private var isUpdating = false
        private var prevFormattedLength = 0

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (!isUpdating) prevFormattedLength = s.length
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            if (isUpdating) return

            val isDeleting = s.length < prevFormattedLength
            val digits = s.filter { it.isDigit() }.take(8).toString()

            val formatted = buildString {
                digits.forEachIndexed { i, c ->
                    append(c)
                    if (i == 1 && !(isDeleting && digits.length == 2)) append('/')
                    if (i == 3 && !(isDeleting && digits.length == 4)) append('/')
                }
            }

            if (formatted == s.toString()) return

            isUpdating = true
            s.replace(0, s.length, formatted)
            isUpdating = false
        }
    }
}

// endregion

// region AccessRequestConsultActivity

class AccessRequestConsultActivity : AppCompatActivity() {

    private val viewModel: AccessRequestConsultViewModel by viewModels()
    private lateinit var binding: ActivityAccessRequestConsultBinding
    private lateinit var adapter: AccessRequestAdapter
    private var detailDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityAccessRequestConsultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        observeViewModel()
        viewModel.loadAccessRequests()
    }

    private fun setupRecyclerView() {
        adapter = AccessRequestAdapter { request -> showDetailDialog(request) }
        binding.rvSolicitudes.layoutManager = LinearLayoutManager(this)
        binding.rvSolicitudes.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.accessRequests.observe(this) { requests ->
            adapter.updateItems(requests)
        }
        viewModel.loadError.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                viewModel.onErrorConsumed()
            }
        }
    }

    private fun showDetailDialog(request: AccessRequest) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_access_request_detail)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(true)

        dialog.findViewById<TextView>(R.id.tvDetailNombres).text = request.nombres
        dialog.findViewById<TextView>(R.id.tvDetailApellidos).text = request.apellidos
        dialog.findViewById<TextView>(R.id.tvDetailCorreo).text = request.correo
        dialog.findViewById<TextView>(R.id.tvDetailDocumento).text = request.documento
        dialog.findViewById<TextView>(R.id.tvDetailFecha).text = request.fecha
        dialog.findViewById<ImageButton>(R.id.btnCloseDetail).setOnClickListener {
            dialog.dismiss()
        }

        detailDialog = dialog
        dialog.show()
    }
}

// endregion
