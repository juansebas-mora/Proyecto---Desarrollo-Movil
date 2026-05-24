package com.example.urumbox.emergencyactivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.urumbox.R
import com.example.urumbox.mapasactivity.MapaActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportarEmergenciaActivity : AppCompatActivity() {

    private var categoriaSeleccionada = ""
    private var gravedadSeleccionada = ""
    private var fotoUri: Uri? = null
    private var cameraUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val galeriaLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fotoUri = it
            mostrarPreview(it)
        }
    }

    private val camaraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let {
                fotoUri = it
                mostrarPreview(it)
            }
        }
    }

    private val permisoCamaraLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) abrirCamara()
        else Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportar_emergencia)

        val btnIncendio = findViewById<AppCompatButton>(R.id.btnIncendio)
        val btnMedica   = findViewById<AppCompatButton>(R.id.btnMedica)
        val btnRobo     = findViewById<AppCompatButton>(R.id.btnRobo)
        val btnSismo    = findViewById<AppCompatButton>(R.id.btnSismo)
        val btnOtro     = findViewById<AppCompatButton>(R.id.btnOtro)
        val botonesCategoria = listOf(btnIncendio, btnMedica, btnRobo, btnSismo, btnOtro)

        val btnBaja  = findViewById<AppCompatButton>(R.id.btnBaja)
        val btnMedia = findViewById<AppCompatButton>(R.id.btnMedia)
        val btnAlta  = findViewById<AppCompatButton>(R.id.btnAlta)
        val botonesGravedad = listOf(btnBaja, btnMedia, btnAlta)

        val etDescripcion      = findViewById<EditText>(R.id.etDescripcion)
        val btnSeleccionarMapa = findViewById<LinearLayout>(R.id.btnSeleccionarMapa)
        val btnSubirFoto       = findViewById<LinearLayout>(R.id.btnSubirFoto)
        val btnEnviarReporte   = findViewById<AppCompatButton>(R.id.btnEnviarReporte)
        val imgPreview         = findViewById<ImageView>(R.id.imgFotoPreview)
        val btnEliminarFoto    = findViewById<Button>(R.id.btnEliminarFoto)

        btnIncendio.setOnClickListener { categoriaSeleccionada = "Incendio"; resaltarSeleccion(btnIncendio, botonesCategoria) }
        btnMedica.setOnClickListener   { categoriaSeleccionada = "Médica";   resaltarSeleccion(btnMedica, botonesCategoria) }
        btnRobo.setOnClickListener     { categoriaSeleccionada = "Robo";     resaltarSeleccion(btnRobo, botonesCategoria) }
        btnSismo.setOnClickListener    { categoriaSeleccionada = "Sismo";    resaltarSeleccion(btnSismo, botonesCategoria) }
        btnOtro.setOnClickListener     { categoriaSeleccionada = "Otro";     resaltarSeleccion(btnOtro, botonesCategoria) }

        btnBaja.setOnClickListener  { gravedadSeleccionada = "Baja";  resaltarSeleccion(btnBaja, botonesGravedad) }
        btnMedia.setOnClickListener { gravedadSeleccionada = "Media"; resaltarSeleccion(btnMedia, botonesGravedad) }
        btnAlta.setOnClickListener  { gravedadSeleccionada = "Alta";  resaltarSeleccion(btnAlta, botonesGravedad) }

        btnSeleccionarMapa.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            intent.putExtra("id_ruta", "ruta_claustro_test")
            startActivity(intent)
        }

        btnSubirFoto.setOnClickListener { mostrarDialogoFoto() }

        btnEliminarFoto.setOnClickListener {
            fotoUri = null
            cameraUri = null
            imgPreview.setImageURI(null)
            imgPreview.visibility = View.GONE
            btnEliminarFoto.visibility = View.GONE
        }

        btnEnviarReporte.setOnClickListener {
            when {
                categoriaSeleccionada.isEmpty() ->
                    Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
                gravedadSeleccionada.isEmpty() ->
                    Toast.makeText(this, "Selecciona el nivel de gravedad", Toast.LENGTH_SHORT).show()
                etDescripcion.text.toString().trim().isEmpty() ->
                    Toast.makeText(this, "Escribe una descripción", Toast.LENGTH_SHORT).show()
                else -> enviarReporte(etDescripcion.text.toString().trim())
            }
        }
    }

    private fun enviarReporte(descripcion: String) {
        val btnEnviarReporte = findViewById<AppCompatButton>(R.id.btnEnviarReporte)
        btnEnviarReporte.isEnabled = false
        btnEnviarReporte.text = "Enviando..."

        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val usuarioId = auth.currentUser?.uid ?: "anonimo"

        val reporte = hashMapOf(
            "categoria" to categoriaSeleccionada,
            "gravedad" to gravedadSeleccionada,
            "descripcion" to descripcion,
            "fecha" to fecha,
            "usuarioId" to usuarioId,
            "estado" to "pendiente"
        )

        db.collection("reportes")
            .add(reporte)
            .addOnSuccessListener {
                mostrarDialogoExito()
            }
            .addOnFailureListener { e ->
                btnEnviarReporte.isEnabled = true
                btnEnviarReporte.text = "Enviar Reporte"
                Toast.makeText(this, "Error al enviar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoFoto() {
        AlertDialog.Builder(this)
            .setTitle("Agregar foto")
            .setItems(arrayOf("📷 Tomar foto", "🖼️ Elegir de galería")) { _, which ->
                when (which) {
                    0 -> verificarPermisoCamara()
                    1 -> galeriaLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            abrirCamara()
        } else {
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamara() {
        try {
            val archivoFoto = File(cacheDir, "foto_emergencia_${System.currentTimeMillis()}.jpg")
            cameraUri = FileProvider.getUriForFile(
                this,
                "com.example.urumbox.fileprovider",
                archivoFoto
            )
            camaraLauncher.launch(cameraUri!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, e.javaClass.simpleName + ": " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarPreview(uri: Uri) {
        val imgPreview = findViewById<ImageView>(R.id.imgFotoPreview)
        val btnEliminarFoto = findViewById<Button>(R.id.btnEliminarFoto)
        imgPreview.setImageURI(uri)
        imgPreview.visibility = View.VISIBLE
        btnEliminarFoto.visibility = View.VISIBLE
    }

    private fun mostrarDialogoExito() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_reporte_publicado, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<AppCompatButton>(R.id.btnCerrarDialog).setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }

    private fun resaltarSeleccion(seleccionado: AppCompatButton, todos: List<AppCompatButton>) {
        todos.forEach { btn ->
            if (btn == seleccionado) {
                btn.setBackgroundResource(R.drawable.rounded_button_background)
                btn.setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                btn.setBackgroundResource(R.drawable.chip_unselected_bg)
                btn.setTextColor(resources.getColor(android.R.color.holo_blue_dark, null))
            }
        }
    }
}