package com.example.urumbox.accessactivity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urumbox.R
import com.example.urumbox.data.model.AccessRequest
import com.example.urumbox.data.repository.QrException
import com.example.urumbox.databinding.ActivityAccessHistoryBinding
import com.example.urumbox.databinding.ActivityAccessMainBinding
import com.example.urumbox.databinding.ActivityAccessQrBinding
import com.example.urumbox.databinding.ActivityAccessRequestBinding
import com.example.urumbox.databinding.ActivityAccessRequestConsultBinding
import com.example.urumbox.databinding.ActivityQrScannerBinding
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

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
            showQrOptionsDialog()
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

    private fun showQrOptionsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_qr_options)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(true)

        dialog.findViewById<MaterialButton>(R.id.btnVerMiQr).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, AccessQrActivity::class.java))
        }
        dialog.findViewById<MaterialButton>(R.id.btnLeerQr).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, QrScannerActivity::class.java))
        }

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

        viewModel.loadAndGenerateQr()

        viewModel.qrBitmap.observe(this) { bitmap ->
            if (bitmap != null) binding.ivQrCode.setImageBitmap(bitmap)
        }
        viewModel.validDate.observe(this) { date ->
            binding.tvValidDate.text = date
        }
        viewModel.loadError.observe(this) { error ->
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
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

// region QrScannerActivity

class QrScannerActivity : AppCompatActivity() {

    private val viewModel: QrScannerViewModel by viewModels()
    private lateinit var binding: ActivityQrScannerBinding
    private var cameraProvider: ProcessCameraProvider? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var scanHandled = false

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera() else finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCloseScanner.setOnClickListener { finish() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }

        viewModel.validationResult.observe(this) { result ->
            result ?: return@observe
            when (result) {
                is QrValidationResult.Success -> showSuccessDialog(result.userData.nombre)
                is QrValidationResult.Error -> showErrorDialog(result.type)
            }
            viewModel.onResultConsumed()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
            )
            analysisUseCase = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also { analysis ->
                    analysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { proxy ->
                        processImageProxy(scanner, proxy)
                    }
                }
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysisUseCase!!
            )
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImageProxy(scanner: BarcodeScanner, imageProxy: ImageProxy) {
        if (scanHandled) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val rawValue = barcodes.firstOrNull()?.rawValue
                if (!rawValue.isNullOrEmpty() && !scanHandled) {
                    scanHandled = true
                    analysisUseCase?.clearAnalyzer()
                    viewModel.validateQrContent(rawValue)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun showSuccessDialog(userName: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_qr_success)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(true)
        dialog.findViewById<TextView>(R.id.tvSuccessUserName).text = userName
        dialog.findViewById<MaterialButton>(R.id.btnAceptar).setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }

    private fun showErrorDialog(error: QrException) {
        val msg = when (error) {
            is QrException.InvalidFormat ->
                "El contenido escaneado no es un código QR de acceso válido."
            is QrException.NotFound ->
                "Este código QR no corresponde a ningún usuario registrado."
            is QrException.Expired ->
                "Este código QR ha expirado. El usuario debe generar uno nuevo."
        }
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_qr_error)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(true)
        dialog.findViewById<TextView>(R.id.tvErrorMessage).text = msg
        dialog.findViewById<MaterialButton>(R.id.btnEntendido).setOnClickListener {
            dialog.dismiss()
            scanHandled = false
            startCamera()
        }
        dialog.show()
    }
}

// endregion
