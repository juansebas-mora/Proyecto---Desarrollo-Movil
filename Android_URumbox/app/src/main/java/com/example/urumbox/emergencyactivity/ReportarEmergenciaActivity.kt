package com.example.urumbox.emergencyactivity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.urumbox.R
import com.example.urumbox.ui.objetosperdidos.MapaBottomSheet
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ReportarEmergenciaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance("gs://urumbox-1c6c1.firebasestorage.app")

    private var categoriaSeleccionada: String = ""
    private var gravedadSeleccionada: String = ""
    private var fotoUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private var latitudSeleccionada: Double = 0.0
    private var longitudSeleccionada: Double = 0.0
    private var ubicacionTexto: String = ""
    private val CAMERA_PERMISSION_REQUEST = 1002

    private val idsCategorias = listOf(
        R.id.btnIncendio, R.id.btnMedica, R.id.btnRobo, R.id.btnSismo, R.id.btnOtro
    )
    private val idsGravedad = listOf(
        R.id.btnBaja, R.id.btnMedia, R.id.btnAlta
    )

    private val galeriaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fotoUri = result.data?.data
                fotoUri?.let { mostrarPreviewFoto(it) }
            }
        }

    private val camaraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cameraImageUri?.let {
                    fotoUri = it
                    mostrarPreviewFoto(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportar_emergencia)

        findViewById<com.example.urumbox.TopbarView>(R.id.topBar)
            .setOnBackClickListener { finish() }

        configurarBotonesCategorias()
        configurarBotonesGravedad()
        configurarFoto()
        configurarMapa()

        findViewById<AppCompatButton>(R.id.btnEnviarReporte).setOnClickListener {
            enviarReporte()
        }
    }

    private fun configurarBotonesCategorias() {
        val mapa = mapOf(
            R.id.btnIncendio to "Incendio",
            R.id.btnMedica to "Médica",
            R.id.btnRobo to "Robo",
            R.id.btnSismo to "Sismo"
        )
        mapa.forEach { (id, nombre) ->
            findViewById<AppCompatButton>(id).setOnClickListener {
                categoriaSeleccionada = nombre
                marcarSeleccionado(idsCategorias, id)
            }
        }
        findViewById<AppCompatButton>(R.id.btnOtro).setOnClickListener {
            mostrarDialogOtro()
        }
    }

    private fun configurarBotonesGravedad() {
        val mapa = mapOf(
            R.id.btnBaja to "Baja",
            R.id.btnMedia to "Media",
            R.id.btnAlta to "Alta"
        )
        mapa.forEach { (id, nivel) ->
            findViewById<AppCompatButton>(id).setOnClickListener {
                gravedadSeleccionada = nivel
                marcarSeleccionado(idsGravedad, id)
            }
        }
    }

    private fun marcarSeleccionado(grupo: List<Int>, seleccionado: Int) {
        grupo.forEach { id ->
            val btn = findViewById<AppCompatButton>(id)
            if (id == seleccionado) {
                btn.setBackgroundResource(R.drawable.rounded_button_background)
                btn.setTextColor(android.graphics.Color.WHITE)
            } else {
                btn.setBackgroundResource(R.drawable.chip_unselected_bg)
                btn.setTextColor(android.graphics.Color.parseColor("#1B3E63"))
            }
        }
    }

    private fun mostrarDialogOtro() {
        val dialogView = layoutInflater.inflate(R.layout.categoria_otro, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val etCategoriaOtro = dialogView.findViewById<EditText>(R.id.etCategoriaOtro)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarOtro)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmarOtro)

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            val texto = etCategoriaOtro.text.toString().trim()
            if (texto.isNotEmpty()) {
                categoriaSeleccionada = "Otro: $texto"
                marcarSeleccionado(idsCategorias, R.id.btnOtro)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Escribe el tipo de emergencia", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun configurarMapa() {
        findViewById<LinearLayout>(R.id.btnSeleccionarMapa).setOnClickListener {
            val mapaSheet = MapaBottomSheet()
            mapaSheet.onUbicacionSeleccionada = { lat, lng, direccion ->
                latitudSeleccionada = lat
                longitudSeleccionada = lng
                ubicacionTexto = direccion
                val tvUbicacion = findViewById<TextView>(R.id.tvUbicacionSeleccionada)
                tvUbicacion?.text = "📍 $direccion"
                tvUbicacion?.visibility = View.VISIBLE
            }
            mapaSheet.show(supportFragmentManager, MapaBottomSheet.TAG)
        }
    }

    private fun configurarFoto() {
        findViewById<LinearLayout>(R.id.btnSubirFoto).setOnClickListener {
            mostrarDialogFuente()
        }
        findViewById<Button>(R.id.btnEliminarFoto).setOnClickListener {
            fotoUri = null
            cameraImageUri = null
            findViewById<ImageView>(R.id.imgFotoPreview).apply {
                setImageURI(null)
                visibility = View.GONE
            }
            it.visibility = View.GONE
        }
    }

    private fun mostrarDialogFuente() {
        AlertDialog.Builder(this)
            .setTitle("Foto de la emergencia")
            .setItems(arrayOf("📷 Tomar foto", "🖼️ Elegir de galería")) { _, which ->
                when (which) {
                    0 -> abrirCamara()
                    1 -> {
                        val intent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        galeriaLauncher.launch(intent)
                    }
                }
            }
            .show()
    }

    private fun abrirCamara() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
            return
        }

        val photoFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "emergencia_${System.currentTimeMillis()}.jpg"
        )
        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        cameraImageUri?.let { camaraLauncher.launch(it) }
    }

    private fun mostrarPreviewFoto(uri: Uri) {
        val preview = findViewById<ImageView>(R.id.imgFotoPreview)
        val btnEliminar = findViewById<Button>(R.id.btnEliminarFoto)
        preview.setImageURI(uri)
        preview.visibility = View.VISIBLE
        btnEliminar.visibility = View.VISIBLE
    }

    private fun enviarReporte() {
        val descripcion = findViewById<EditText>(R.id.etDescripcion).text.toString().trim()

        if (categoriaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
            return
        }
        if (gravedadSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona el nivel de gravedad", Toast.LENGTH_SHORT).show()
            return
        }
        if (descripcion.isEmpty()) {
            Toast.makeText(this, "Escribe una descripción", Toast.LENGTH_SHORT).show()
            return
        }

        val btnEnviar = findViewById<AppCompatButton>(R.id.btnEnviarReporte)
        btnEnviar.isEnabled = false
        btnEnviar.text = "Enviando…"

        if (fotoUri != null) {
            subirFotoYGuardar(fotoUri!!, descripcion, btnEnviar)
        } else {
            guardarEnFirestore("", descripcion, btnEnviar)
        }
    }

    // ✅ CAMBIO: putStream en lugar de putFile — compatible con file:// y content://
    private fun subirFotoYGuardar(uri: Uri, descripcion: String, btnEnviar: AppCompatButton) {
        val nombreArchivo = "emergencias/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(nombreArchivo)

        val inputStream = try {
            contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo leer la foto: ${e.message}", Toast.LENGTH_LONG).show()
            btnEnviar.isEnabled = true
            btnEnviar.text = "Enviar Reporte"
            return
        }

        if (inputStream == null) {
            Toast.makeText(this, "La foto no está disponible", Toast.LENGTH_SHORT).show()
            btnEnviar.isEnabled = true
            btnEnviar.text = "Enviar Reporte"
            return
        }

        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        ref.putStream(inputStream, metadata)
            .addOnSuccessListener {
                inputStream.close()
                ref.downloadUrl
                    .addOnSuccessListener { urlDescarga ->
                        guardarEnFirestore(urlDescarga.toString(), descripcion, btnEnviar)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error obteniendo URL: ${e.message}", Toast.LENGTH_LONG).show()
                        btnEnviar.isEnabled = true
                        btnEnviar.text = "Enviar Reporte"
                    }
            }
            .addOnFailureListener { e ->
                inputStream.close()
                Toast.makeText(this, "Error subiendo foto: ${e.message}", Toast.LENGTH_LONG).show()
                btnEnviar.isEnabled = true
                btnEnviar.text = "Enviar Reporte"
            }
    }

    private fun guardarEnFirestore(urlFoto: String, descripcion: String, btnEnviar: AppCompatButton) {
        val ahora = Date()
        val fechaFormateada = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(ahora)

        val reporte = hashMapOf(
            "categoria"   to categoriaSeleccionada,
            "gravedad"    to gravedadSeleccionada,
            "descripcion" to descripcion,
            "fecha"       to fechaFormateada,
            "timestamp"   to Timestamp(ahora),
            "latitud"     to latitudSeleccionada,
            "longitud"    to longitudSeleccionada,
            "ubicacion"   to ubicacionTexto,
            "fotoUrl"     to urlFoto
        )

        db.collection("reportes")
            .add(reporte)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Reporte enviado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                btnEnviar.isEnabled = true
                btnEnviar.text = "Enviar Reporte"
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            abrirCamara()
        }
    }
}