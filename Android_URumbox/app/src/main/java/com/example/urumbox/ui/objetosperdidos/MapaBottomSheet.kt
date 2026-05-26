package com.example.urumbox.ui.objetosperdidos

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.urumbox.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

class MapaBottomSheet : BottomSheetDialogFragment(), OnMapReadyCallback {

    // Callback para devolver la ubicación al fragment padre
    var onUbicacionSeleccionada: ((latitud: Double, longitud: Double, direccion: String) -> Unit)? = null

    private lateinit var mMap: GoogleMap
    private lateinit var tvDireccion: TextView
    private lateinit var btnConfirmar: Button

    private var latSeleccionada: Double = 0.0
    private var lngSeleccionada: Double = 0.0
    private var hayMarcador = false

    private val ubicacionCampus = LatLng(4.6275, -74.0653)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST) { }

        return inflater.inflate(R.layout.bottom_sheet_mapa, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDireccion  = view.findViewById(R.id.tvDireccionDetectada)
        btnConfirmar = view.findViewById(R.id.btnConfirmarUbicacion)

        // Inicializar mapa
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnConfirmar.setOnClickListener {
            if (hayMarcador) {
                onUbicacionSeleccionada?.invoke(
                    latSeleccionada,
                    lngSeleccionada,
                    tvDireccion.text.toString()
                )
                dismiss()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Centra en el campus
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionCampus, 16f))

        // Toque en mapa → marcador
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Lugar del objeto")
            )

            latSeleccionada = latLng.latitude
            lngSeleccionada = latLng.longitude
            hayMarcador = true
            btnConfirmar.isEnabled = true

            // Geocodificación
            val geocoder = Geocoder(requireContext(), Locale("es", "CO"))

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                // API 33+ → callback asíncrono
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { resultados ->
                    val dir = resultados.firstOrNull()?.getAddressLine(0)
                        ?: "Lat: %.5f, Lng: %.5f".format(latLng.latitude, latLng.longitude)
                    requireActivity().runOnUiThread {
                        tvDireccion.text = dir
                    }
                }
            } else {
                Thread {
                    try {
                        @Suppress("DEPRECATION")
                        val resultados = geocoder.getFromLocation(
                            latLng.latitude, latLng.longitude, 1
                        )
                        val dir = resultados?.firstOrNull()?.getAddressLine(0)
                            ?: "Lat: %.5f, Lng: %.5f".format(latLng.latitude, latLng.longitude)
                        requireActivity().runOnUiThread {
                            tvDireccion.text = dir
                        }
                    } catch (e: Exception) {
                        requireActivity().runOnUiThread {
                            tvDireccion.text = "Lat: %.5f, Lng: %.5f"
                                .format(latLng.latitude, latLng.longitude)
                        }
                    }
                }.start()
            }
        }
    }

    companion object {
        const val TAG = "MapaBottomSheet"
    }
}