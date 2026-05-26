package com.example.urumbox.useractivity

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urumbox.R
import com.example.urumbox.databinding.ActivityGestionUsuariosBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class GestionUsuariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionUsuariosBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: UsuarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemBarColor = getColor(R.color.azul_ur)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(systemBarColor),
            navigationBarStyle = SystemBarStyle.dark(systemBarColor)
        )

        binding = ActivityGestionUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.topBar.setOnBackClickListener { finish() }

        adapter = UsuarioAdapter(mutableListOf()) { usuario ->
            mostrarMenuOpciones(usuario)
        }
        binding.recyclerUsuarios.layoutManager = LinearLayoutManager(this)
        binding.recyclerUsuarios.adapter = adapter

        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        db.collection("usuarios").get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.id
                    val nombre = doc.getString("nombreCompleto") ?: ""
                    val correo = doc.getString("correo") ?: ""
                    val rol = doc.getString("rol") ?: ""
                    val estado = doc.getString("estado") ?: "activo"
                    val foto = doc.getString("fotoPerfil")
                    UsuarioItem(uid, nombre, correo, rol, estado, foto)
                }
                adapter.updateItems(lista)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarMenuOpciones(usuario: UsuarioItem) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_opciones_usuario)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(true)

        dialog.findViewById<TextView>(R.id.tvNombreUsuarioDialog).text = usuario.nombreCompleto
        dialog.findViewById<TextView>(R.id.tvTextoInactivar).text =
            if (usuario.estado == "activo") "Inactivar usuario" else "Activar usuario"

        dialog.findViewById<LinearLayout>(R.id.itemAsignarRol).setOnClickListener {
            dialog.dismiss()
            mostrarDialogoAsignarRol(usuario)
        }
        dialog.findViewById<LinearLayout>(R.id.itemInactivarUsuario).setOnClickListener {
            dialog.dismiss()
            cambiarEstado(usuario)
        }
        dialog.findViewById<LinearLayout>(R.id.itemEliminarUsuario).setOnClickListener {
            dialog.dismiss()
            confirmarEliminacion(usuario)
        }
        dialog.findViewById<Button>(R.id.btnCancelarOpciones).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDialogoAsignarRol(usuario: UsuarioItem) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_asignar_rol)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(true)

        fun asignarRol(rol: String) {
            db.collection("usuarios").document(usuario.uid)
                .update("rol", rol)
                .addOnSuccessListener {
                    Toast.makeText(this, "Rol asignado correctamente", Toast.LENGTH_SHORT).show()
                    cargarUsuarios()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al asignar rol", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }

        dialog.findViewById<MaterialButton>(R.id.btnRolAdministrador).setOnClickListener { asignarRol("Administrador") }
        dialog.findViewById<MaterialButton>(R.id.btnRolUsuarioUR).setOnClickListener { asignarRol("Usuario UR") }
        dialog.findViewById<MaterialButton>(R.id.btnRolVigilante).setOnClickListener { asignarRol("Vigilante") }
        dialog.findViewById<MaterialButton>(R.id.btnCancelar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun cambiarEstado(usuario: UsuarioItem) {
        val inactivar = usuario.estado == "activo"
        val nuevoEstado = if (inactivar) "inactivo" else "activo"

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_inactivar_usuario)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(true)

        dialog.findViewById<android.widget.TextView>(R.id.tvTituloInactivar).text =
            if (inactivar) "Inactivar usuario" else "Activar usuario"
        dialog.findViewById<android.widget.TextView>(R.id.tvMensajeInactivar).text =
            if (inactivar) "¿Deseas inactivar a ${usuario.nombreCompleto}? No podrá acceder a la aplicación."
            else "¿Deseas activar a ${usuario.nombreCompleto}? Recuperará el acceso a la aplicación."

        val btnConfirmar = dialog.findViewById<MaterialButton>(R.id.btnConfirmarInactivar)
        val colorRes = if (inactivar) R.color.rojo_ur else R.color.text_bajo
        btnConfirmar.backgroundTintList = ContextCompat.getColorStateList(this, colorRes)
        btnConfirmar.text = if (inactivar) "Inactivar" else "Activar"

        btnConfirmar.setOnClickListener {
            db.collection("usuarios").document(usuario.uid)
                .update("estado", nuevoEstado)
                .addOnSuccessListener {
                    val msg = if (nuevoEstado == "inactivo") "Usuario inactivado" else "Usuario activado"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    cargarUsuarios()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cambiar estado", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }
        dialog.findViewById<MaterialButton>(R.id.btnCancelarInactivar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun confirmarEliminacion(usuario: UsuarioItem) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_eliminar_usuario)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.6f)
        dialog.setCancelable(true)

        dialog.findViewById<MaterialButton>(R.id.btnConfirmarEliminar).setOnClickListener {
            db.collection("usuarios").document(usuario.uid)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show()
                    cargarUsuarios()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }
        dialog.findViewById<MaterialButton>(R.id.btnCancelarEliminar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
