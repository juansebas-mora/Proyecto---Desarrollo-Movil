package com.example.urumbox.useractivity

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.urumbox.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InfoPersonalActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_personal)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        loadUserData()
        setupPhoneEdit()
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        db.collection("usuarios").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val nombreCompleto = doc.getString("nombreCompleto")
                    ?: "${doc.getString("nombre") ?: ""} ${doc.getString("apellido") ?: ""}".trim()
                val correo = doc.getString("correo")
                    ?: doc.getString("email")
                    ?: user.email
                    ?: ""
                val usuario = doc.getString("usuario")
                    ?: correo.substringBefore("@")
                val rol = doc.getString("rol") ?: ""

                findViewById<TextView>(R.id.tvNombreValor).text = nombreCompleto
                findViewById<TextView>(R.id.tvUsuarioValor).text = usuario
                findViewById<TextView>(R.id.tvCorreoValor).text = correo
                findViewById<TextView>(R.id.tvRolValor).text = rol
                findViewById<EditText>(R.id.etTelefono).setText(doc.getString("telefono") ?: "")
                findViewById<TextView>(R.id.tvNacimientoValor).text = doc.getString("fechaNacimiento") ?: ""
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar información personal", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupPhoneEdit() {
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val ivEdit = findViewById<ImageView>(R.id.ivEditTelefono)
        val ivSave = findViewById<ImageView>(R.id.ivGuardarTelefono)

        ivEdit.setOnClickListener {
            etTelefono.isEnabled = true
            etTelefono.requestFocus()
            ivEdit.visibility = View.GONE
            ivSave.visibility = View.VISIBLE
        }

        ivSave.setOnClickListener {
            val newPhone = etTelefono.text.toString().trim()
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("usuarios").document(uid)
                .update("telefono", newPhone)
                .addOnSuccessListener {
                    etTelefono.isEnabled = false
                    ivEdit.visibility = View.VISIBLE
                    ivSave.visibility = View.GONE
                    Toast.makeText(this, "Teléfono actualizado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar el teléfono", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
