package com.example.urumbox.emergencyactivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.urumbox.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class EmergenciasActivity : AppCompatActivity(), OnMapReadyCallback {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_emergencias)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<com.example.urumbox.TopbarView>(R.id.topBar)
            .setOnBackClickListener { finish() }

        findViewById<CardView>(R.id.btnRutasEvacuacion).setOnClickListener {
            startActivity(Intent(this, RutaEvacuacionActivity::class.java))
        }

        findViewById<CardView>(R.id.btnPuntosEncuentro).setOnClickListener {
            startActivity(Intent(this, PuntosEncuentroActivity::class.java))
        }

        findViewById<AppCompatButton>(R.id.btnReportarEmergencia).setOnClickListener {
            startActivity(Intent(this, ReportarEmergenciaActivity::class.java))
        }

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.apply {
            isZoomControlsEnabled = false
            isScrollGesturesEnabled = false
            isZoomGesturesEnabled = false
            isTiltGesturesEnabled = false
            isRotateGesturesEnabled = false
        }
        mostrarUbicacionActual()
    }

    private fun mostrarUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        googleMap?.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                googleMap?.clear()
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Tu ubicación actual")
                )
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, 17f)
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            mostrarUbicacionActual()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        cargarUltimosReportes()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun cargarUltimosReportes() {
        val contenedor    = findViewById<LinearLayout>(R.id.llReportesRecientes) ?: return
        val tvSinReportes = findViewById<TextView>(R.id.tvSinReportes)

        contenedor.visibility     = View.GONE
        tvSinReportes?.visibility = View.VISIBLE
        tvSinReportes?.text       = "Cargando reportes…"

        db.collection("reportes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { snapshot ->
                contenedor.removeAllViews()

                if (snapshot.isEmpty) {
                    tvSinReportes?.text       = "No hay reportes recientes"
                    tvSinReportes?.visibility = View.VISIBLE
                    contenedor.visibility     = View.GONE
                    return@addOnSuccessListener
                }

                tvSinReportes?.visibility = View.GONE
                contenedor.visibility     = View.VISIBLE

                for (doc in snapshot.documents) {
                    val categoria   = doc.getString("categoria")   ?: "Sin categoría"
                    val gravedad    = doc.getString("gravedad")    ?: ""
                    val descripcion = doc.getString("descripcion") ?: ""
                    val fecha       = doc.getString("fecha")       ?: ""
                    val ubicacion   = doc.getString("ubicacion")   ?: "No especificada"
                    // ✅ corregido: fotoUri → fotoUrl
                    val fotoUrl     = doc.getString("fotoUrl")     ?: ""

                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_reporte_reciente, contenedor, false)

                    itemView.findViewById<TextView>(R.id.tvReporteCategoria).text =
                        emojiParaCategoria(categoria) + "  $categoria"

                    itemView.findViewById<TextView>(R.id.tvReporteGravedad).also { tv ->
                        tv.text = gravedad
                        tv.setTextColor(colorParaGravedad(gravedad))
                    }

                    itemView.findViewById<TextView>(R.id.tvReporteDescripcion).text =
                        if (descripcion.length > 80) descripcion.take(80) + "…"
                        else descripcion

                    itemView.findViewById<TextView>(R.id.tvReporteFecha).text = fecha

                    val ivFoto = itemView.findViewById<ImageView>(R.id.ivReporteFoto)
                    // ✅ ahora carga desde URL de Firebase Storage con Glide
                    cargarFoto(ivFoto, fotoUrl)

                    itemView.setOnClickListener {
                        mostrarDetalleReporte(
                            categoria, gravedad, descripcion,
                            fecha, ubicacion, fotoUrl
                        )
                    }

                    contenedor.addView(itemView)
                }
            }
            .addOnFailureListener {
                tvSinReportes?.text       = "Error al cargar reportes"
                tvSinReportes?.visibility = View.VISIBLE
                contenedor.visibility     = View.GONE
            }
    }

    // ✅ ahora usa Glide para cargar URLs https:// de Firebase Storage
    private fun cargarFoto(ivFoto: ImageView, fotoUrl: String) {
        if (fotoUrl.isNotEmpty()) {
            ivFoto.visibility = View.VISIBLE
            Glide.with(this)
                .load(fotoUrl)
                .placeholder(R.drawable.ic_image_placeholder) // opcional
                .error(R.drawable.ic_image_placeholder)       // opcional
                .into(ivFoto)
        } else {
            ivFoto.visibility = View.GONE
        }
    }

    private fun mostrarDetalleReporte(
        categoria: String,
        gravedad: String,
        descripcion: String,
        fecha: String,
        ubicacion: String = "No especificada",
        fotoUrl: String = ""
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_detalle_reporte, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        dialogView.findViewById<TextView>(R.id.tvDetalleCategoria).text =
            emojiParaCategoria(categoria) + "  $categoria"

        dialogView.findViewById<TextView>(R.id.tvDetalleGravedad).apply {
            text = gravedad
            setTextColor(colorParaGravedad(gravedad))
        }

        dialogView.findViewById<TextView>(R.id.tvDetalleDescripcion).text = descripcion
        dialogView.findViewById<TextView>(R.id.tvDetalleFecha).text       = fecha
        dialogView.findViewById<TextView>(R.id.tvDetalleUbicacion).text   =
            ubicacion.ifEmpty { "No especificada" }

        val ivDetalleFoto = dialogView.findViewById<ImageView>(R.id.ivDetalleFoto)
        val tvFotoLabel   = dialogView.findViewById<TextView>(R.id.tvDetalleFotoLabel)

        cargarFoto(ivDetalleFoto, fotoUrl)
        tvFotoLabel.visibility = ivDetalleFoto.visibility

        dialogView.findViewById<AppCompatButton>(R.id.btnCerrarDetalle)
            .setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun emojiParaCategoria(categoria: String): String = when {
        categoria.startsWith("Incendio") -> "🔥"
        categoria.startsWith("Médica")   -> "🚑"
        categoria.startsWith("Robo")     -> "🚨"
        categoria.startsWith("Sismo")    -> "🌍"
        else                             -> "⚠️"
    }

    private fun colorParaGravedad(gravedad: String): Int = when (gravedad) {
        "Alta"  -> getColor(android.R.color.holo_red_dark)
        "Media" -> getColor(android.R.color.holo_orange_dark)
        "Baja"  -> getColor(android.R.color.holo_green_dark)
        else    -> getColor(android.R.color.darker_gray)
    }
}