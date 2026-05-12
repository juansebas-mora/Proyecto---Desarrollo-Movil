package com.example.urumbox.useractivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.urumbox.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class PerfilActivity : AppCompatActivity() {

    private lateinit var imgAvatar: ImageView
    private lateinit var tvNombre: TextView
    private lateinit var tvRol: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var cameraImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { uploadProfilePhoto(it) } }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraImageUri?.let { uploadProfilePhoto(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        imgAvatar = findViewById(R.id.imgAvatar)
        tvNombre = findViewById(R.id.tvNombre)
        tvRol = findViewById(R.id.tvRol)

        loadUserData()

        imgAvatar.setOnClickListener { showPhotoSourceDialog() }

        findViewById<LinearLayout>(R.id.itemInfoPersonal).setOnClickListener {
            startActivity(Intent(this, InfoPersonalActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.itemConfiguracion).setOnClickListener {
            startActivity(Intent(this, ConfiguracionActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.itemCerrarSesion).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, InicioActivity::class.java))
            finishAffinity()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        db.collection("usuarios").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val nombreCompleto = doc.getString("nombreCompleto")
                    ?: "${doc.getString("nombre") ?: ""} ${doc.getString("apellido") ?: ""}".trim()
                tvNombre.text = nombreCompleto.ifEmpty { user.email ?: "" }
                tvRol.text = doc.getString("rol") ?: ""
                val fotoUrl = doc.getString("fotoPerfil")
                if (!fotoUrl.isNullOrEmpty()) {
                    Glide.with(this).load(fotoUrl).circleCrop().into(imgAvatar)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPhotoSourceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Foto de perfil")
            .setItems(arrayOf("Tomar foto", "Elegir de galería")) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun openCamera() {
        val photoFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "profile_${System.currentTimeMillis()}.jpg"
        )
        cameraImageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        cameraImageUri?.let { uri ->
            cameraLauncher.launch(uri)
        }
    }

    private fun uploadProfilePhoto(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("fotos_perfil/$uid.jpg")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri: Uri ->
                    db.collection("usuarios").document(uid)
                        .update("fotoPerfil", downloadUri.toString())
                        .addOnSuccessListener {
                            Glide.with(this).load(downloadUri).circleCrop().into(imgAvatar)
                            Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar URL de foto", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir la foto", Toast.LENGTH_SHORT).show()
            }
    }
}
