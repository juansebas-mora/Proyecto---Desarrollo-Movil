package com.example.urumbox.useractivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.urumbox.R

@Suppress("DEPRECATION")
class PermisosActivity : AppCompatActivity() {

    private lateinit var switchUbicacion: Switch
    private lateinit var switchNotificaciones: Switch
    private lateinit var switchCamara: Switch

    private var isUpdatingSwitch = false

    companion object {
        private const val REQ_UBICACION = 100
        private const val REQ_CAMARA = 101
        private const val REQ_NOTIFICACIONES = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permisos)

        switchUbicacion = findViewById(R.id.switchUbicacion)
        switchNotificaciones = findViewById(R.id.switchNotificaciones)
        switchCamara = findViewById(R.id.switchCamara)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        updateSwitchStates()
        setupSwitchListeners()
    }

    override fun onResume() {
        super.onResume()
        updateSwitchStates()
    }

    private fun updateSwitchStates() {
        isUpdatingSwitch = true
        switchUbicacion.isChecked = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        switchCamara.isChecked = hasPermission(Manifest.permission.CAMERA)
        switchNotificaciones.isChecked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else true
        isUpdatingSwitch = false
    }

    private fun hasPermission(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun setupSwitchListeners() {
        switchUbicacion.setOnCheckedChangeListener { _, isChecked ->
            onSwitchChanged(switchUbicacion, isChecked, Manifest.permission.ACCESS_FINE_LOCATION, REQ_UBICACION)
        }
        switchCamara.setOnCheckedChangeListener { _, isChecked ->
            onSwitchChanged(switchCamara, isChecked, Manifest.permission.CAMERA, REQ_CAMARA)
        }
        switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                onSwitchChanged(switchNotificaciones, isChecked, Manifest.permission.POST_NOTIFICATIONS, REQ_NOTIFICACIONES)
            } else if (!isChecked && !isUpdatingSwitch) {
                isUpdatingSwitch = true
                switchNotificaciones.isChecked = true
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
            REQ_UBICACION -> switchUbicacion.isChecked = granted
            REQ_CAMARA -> switchCamara.isChecked = granted
            REQ_NOTIFICACIONES -> switchNotificaciones.isChecked = granted
        }
        isUpdatingSwitch = false
    }

    private fun openAppSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        })
    }
}
