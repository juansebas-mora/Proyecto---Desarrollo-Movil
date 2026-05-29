package com.example.urumbox.emergencyactivity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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

class PuntosEncuentroActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST = 2002

    // Las mismas 4 coordenadas del XML
    private val puntos = listOf(
        Pair("Plaza principal", LatLng(4.600436, -74.073101)),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puntos_encuentro)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<com.example.urumbox.TopbarView>(R.id.topBar)
            .setOnBackClickListener { finish() }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapPuntosEncuentro) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        // Marcadores de puntos de encuentro
        puntos.forEach { (nombre, coords) ->
            mMap.addMarker(
                MarkerOptions()
                    .position(coords)
                    .title(nombre)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
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
                val userLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(userLatLng, 17f)
                )
            } else {
                // Fallback: centrar en los puntos
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntos[0].second, 16f))
                Toast.makeText(this, "No se pudo obtener tu ubicación", Toast.LENGTH_SHORT).show()
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
            pedirUbicacion()
        }
    }
}