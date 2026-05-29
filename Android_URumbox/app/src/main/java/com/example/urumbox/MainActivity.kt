package com.example.urumbox

import com.example.urumbox.emergencyactivity.EmergenciasActivity
import android.content.Intent
import android.content.pm.PackageManager
import com.example.urumbox.notificationactivity.NotificationActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.urumbox.databinding.ActivityMainBinding
import com.example.urumbox.objetosactivity.ObjetosActivity
import com.example.urumbox.mapasactivity.BusquedaActivity
import com.example.urumbox.mapasactivity.MapaActivity
import androidx.activity.viewModels
import android.view.View

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: DIAGNOSTICO TEMPORAL — Eliminar despues de registrar el SHA-1 en Firebase Console
        // Este bloque imprime el SHA-1 del dispositivo en Logcat (tag: SHA1_KEY).
        // Pasos: 1) Corre la app  2) Filtra Logcat por "SHA1_KEY"  3) Copia el SHA-1
        //        4) Firebase Console → Configuracion del proyecto → Tu app Android → Agregar huella digital
        //        5) Elimina este bloque
        try {
            @Suppress("DEPRECATION")
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            @Suppress("DEPRECATION")
            info.signatures?.forEach { signature ->
                val md = java.security.MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT)
                android.util.Log.d("SHA1_KEY", "SHA1: $hashKey")
            }
        } catch (e: Exception) {
            android.util.Log.e("SHA1_KEY", "Error obteniendo SHA1: ${e.message}")
        }

        enableEdgeToEdge()
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Búsqueda
        binding.searchBarDashboard.setOnClickListener {
            val intent = Intent(this, BusquedaActivity::class.java)
            val options = androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                binding.searchBarDashboard,
                "search_bar_transition"
            )
            startActivity(intent, options.toBundle())
        }

        // Búsqueda dedicada de Rutas
        binding.cardBuscarRuta.setOnClickListener {
            val intent = Intent(this, BusquedaActivity::class.java)
            startActivity(intent)
        }

        // Configuración de Notificaciones en Dashboard
        val notiAdapter = com.example.urumbox.notificationactivity.DashboardNotificacionAdapter { noti ->
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        binding.rvDashboardNotifications.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvDashboardNotifications.adapter = notiAdapter

        viewModel.latestNotifications.observe(this) { list ->
            if (list.isEmpty()) {
                binding.cardNoNotifications.visibility = View.VISIBLE
                binding.rvDashboardNotifications.visibility = View.GONE
            } else {
                binding.cardNoNotifications.visibility = View.GONE
                binding.rvDashboardNotifications.visibility = View.VISIBLE
                notiAdapter.submitList(list)
            }
        }

        // Emergencias y Evacuación
        binding.btnSimulacroRoute.setOnClickListener {
            val intent = Intent(this, EmergenciasActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        binding.btnReportIncident.setOnClickListener {
            startActivity(Intent(this, EmergenciasActivity::class.java))
        }

        // Configuración de Objetos Perdidos en Dashboard
        val lostAdapter = com.example.urumbox.objetosactivity.DashboardLostObjectsAdapter { objeto ->
            startActivity(Intent(this, ObjetosActivity::class.java))
        }
        binding.rvDashboardLostObjects.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvDashboardLostObjects.adapter = lostAdapter

        viewModel.latestLostObjects.observe(this) { list ->
            if (list.isEmpty()) {
                binding.cardNoLostObjects.visibility = View.VISIBLE
                binding.rvDashboardLostObjects.visibility = View.GONE
            } else {
                binding.cardNoLostObjects.visibility = View.GONE
                binding.rvDashboardLostObjects.visibility = View.VISIBLE
                lostAdapter.submitList(list)
            }
        }

        binding.btnAddObject.setOnClickListener {
            startActivity(Intent(this, ObjetosActivity::class.java))
        }

        // Topbar
        binding.btnNavNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        binding.btnNavProfile.setOnClickListener {
            startActivity(Intent(this, com.example.urumbox.useractivity.PerfilActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarObjetosPerdidos()
    }
}
