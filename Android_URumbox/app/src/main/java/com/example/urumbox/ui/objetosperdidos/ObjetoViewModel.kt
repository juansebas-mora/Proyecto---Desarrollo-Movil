package com.example.urumbox.ui.objetosperdidos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.urumbox.data.model.objetosperdidos.ObjetoPerdido
import com.example.urumbox.data.repository.objetosperdidos.ObjetoRepository

class ObjetoViewModel : ViewModel() {

    private val repository = ObjetoRepository()

    // ── Lista de objetos ───────────────────────────────────────────────────
    private val _objetos = MutableLiveData<List<ObjetoPerdido>>()
    val objetos: LiveData<List<ObjetoPerdido>> get() = _objetos

    // ── Estado de carga ────────────────────────────────────────────────────
    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    // ── Mensajes de éxito o error ──────────────────────────────────────────
    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> get() = _mensaje

    // ── Datos del usuario autenticado ──────────────────────────────────────
    // Triple: (nombreCompleto, telefono, correo)
    private val _datosUsuario = MutableLiveData<Triple<String, String, String>>()
    val datosUsuario: LiveData<Triple<String, String, String>> get() = _datosUsuario

    // ── CONSULTA: carga los objetos desde Firestore ────────────────────────
    fun cargarObjetos() {
        _cargando.value = true
        repository.obtenerObjetos(
            onExito = { lista ->
                _objetos.value = lista
                _cargando.value = false
            },
            onError = { e ->
                _mensaje.value = "Error al cargar objetos: ${e.message}"
                _cargando.value = false
            }
        )
    }

    // ── CONSULTA USUARIO: carga los datos del usuario autenticado ─────────
    fun cargarDatosUsuario() {
        repository.obtenerDatosUsuarioActual(
            onExito = { nombre, telefono, correo ->
                _datosUsuario.value = Triple(nombre, telefono, correo)
            },
            onError = { e ->
                _mensaje.value = "No se pudieron cargar tus datos: ${e.message}"
                _datosUsuario.value = Triple("", "", "")
            }
        )
    }

    // ── REGISTRO: guarda un nuevo objeto en Firestore ─────────────────────
    fun registrarObjeto(objeto: ObjetoPerdido) {
        _cargando.value = true
        repository.registrarObjeto(
            objeto = objeto,
            onExito = {
                _mensaje.value = "Objeto reportado exitosamente"
                _cargando.value = false
                cargarObjetos()
            },
            onError = { e ->
                _mensaje.value = "Error al reportar objeto: ${e.message}"
                _cargando.value = false
            }
        )
    }
}