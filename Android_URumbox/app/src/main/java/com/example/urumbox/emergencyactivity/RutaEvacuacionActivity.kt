package com.example.urumbox.emergencyactivity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.urumbox.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlin.math.sqrt

class RutaEvacuacionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST = 2001

    // Las mismas 4 salidas del XML
    private val salidas = listOf(
        Triple("A", "Plaza principal",   LatLng(4.600436, -74.073101)),
        Triple("B", "Entrada norte",     LatLng(4.600650, -74.073574)),
        Triple("C", "Salida sur",        LatLng(4.599796, -74.073204)),
        Triple("D", "Salida oeste",      LatLng(4.599539, -74.073610))
    )

    private var ubicacionUsuario: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruta_evacuacion)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<com.example.urumbox.TopbarView>(R.id.topBar)
            .setOnBackClickListener { finish() }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapEvacuacion) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.btnSigueLaRuta).setOnClickListener {
            val origen = ubicacionUsuario
            if (origen == null) {
                Toast.makeText(this, "Esperando ubicación…", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val destino = salidaMasCercana(origen)
            dibujarRuta(origen, destino.third)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origen, 17f))
            Toast.makeText(this, "Ruta a ${destino.second}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        // Marcadores de salidas
        salidas.forEach { (letra, nombre, coords) ->
            mMap.addMarker(
                MarkerOptions()
                    .position(coords)
                    .title("Salida $letra — $nombre")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
        }

        pedirUbicacion()
    }

    private fun pedirUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                ubicacionUsuario = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(ubicacionUsuario!!, 17f)
                )
            } else {
                // Fallback: centrar en el claustro
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(salidas[0].third, 16f))
                Toast.makeText(this, "No se pudo obtener tu ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun salidaMasCercana(origen: LatLng): Triple<String, String, LatLng> {
        return salidas.minByOrNull { (_, _, coords) ->
            distancia(origen, coords)
        }!!
    }

    private fun distancia(a: LatLng, b: LatLng): Double {
        val dx = a.latitude - b.latitude
        val dy = a.longitude - b.longitude
        return sqrt(dx * dx + dy * dy)
    }

    private fun dibujarRuta(origen: LatLng, destino: LatLng) {
        mMap.clear()
        // Volver a poner los marcadores de salida
        salidas.forEach { (letra, nombre, coords) ->
            mMap.addMarker(
                MarkerOptions()
                    .position(coords)
                    .title("Salida $letra — $nombre")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
        }
        // Línea directa origen → destino
        mMap.addPolyline(
            PolylineOptions()
                .add(origen, destino)
                .width(8f)
                .color(Color.parseColor("#4CAF50"))
                .geodesic(true)
        )
        // Marcador del usuario
        mMap.addMarker(
            MarkerOptions()
                .position(origen)
                .title("Tu ubicación")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
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
            pedirUbicacion()
        }
    }
}