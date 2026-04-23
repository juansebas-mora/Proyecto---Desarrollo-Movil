package com.example.urumbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urumbox.databinding.FragmentObjetosPerdidosBinding
import java.util.*

class ObjetosPerdidosFragment : Fragment() {

    private var _binding: FragmentObjetosPerdidosBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ObjetosAdapter

    private var filtroEstado: EstadoObjeto? = null
    private var textoBusqueda: String = ""

    private val listaCompleta: List<ObjetoPerdido> = listOf(
        ObjetoPerdido(
            id = 1,
            nombre = "Carnet Universitario",
            ubicacion = "Biblioteca Central",
            descripcion = "Carnet con foto, nombre Juan Pérez, código 2021115",
            fecha = Calendar.getInstance().apply { add(Calendar.HOUR, -1) }.time,
            estado = EstadoObjeto.PERDIDO,
            categoria = "Documentos",
            nombreReportante   = "Juan Andrés García López",
            telefonoReportante = "+57 310 456 7890",
            correoReportante   = "ja.garcia@urosario.edu.co"
        ),
        ObjetoPerdido(
            id = 2,
            nombre = "Mochila Negra",
            ubicacion = "Terraza Torre 2",
            descripcion = "Mochila Totto negra con cremallera roja",
            fecha = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 15)
                set(Calendar.MINUTE, 15)
            }.time,
            estado = EstadoObjeto.ENCONTRADO
        ),
        ObjetoPerdido(
            id = 3,
            nombre = "Celular Samsung Galaxy",
            ubicacion = "Casur - Salón 208",
            descripcion = "Samsung Galaxy negro con funda transparente. Pantalla sin daños.",
            fecha = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 15); set(Calendar.MINUTE, 15)
            }.time,
            estado = EstadoObjeto.ENCONTRADO,
            categoria = "Tecnología",
            nombreReportante   = "Laura Pérez",
            telefonoReportante = "+57 315 123 4567",
            correoReportante   = "l.perez@urosario.edu.co"
        ),
        ObjetoPerdido(
            id = 4,
            nombre = "Audífonos Bluetooth",
            ubicacion = "Biblioteca Central",
            descripcion = "JBL negros, estuche gris, con nombre escrito en el estuche",
            fecha = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -2)
            }.time,
            estado = EstadoObjeto.PERDIDO
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentObjetosPerdidosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarRecyclerView()
        configurarFiltros()
        configurarBusqueda()
        configurarBotones()
        aplicarFiltros()
    }

    private fun configurarRecyclerView() {
        adapter = ObjetosAdapter(requireContext(), emptyList()) { objeto ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_objetos, DetalleObjetoFragment.newInstance(objeto))
                .addToBackStack(null)
                .commit()
        }
        binding.recyclerViewObjetos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewObjetos.adapter = adapter
    }

    private fun configurarFiltros() {
        binding.chipTodos.setOnClickListener {
            binding.chipTodos.isChecked = true
            binding.chipPerdidos.isChecked = false
            binding.chipEncontrados.isChecked = false
            filtroEstado = null
            aplicarFiltros()
        }
        binding.chipPerdidos.setOnClickListener {
            binding.chipTodos.isChecked = false
            binding.chipPerdidos.isChecked = true
            binding.chipEncontrados.isChecked = false
            filtroEstado = EstadoObjeto.PERDIDO
            aplicarFiltros()
        }
        binding.chipEncontrados.setOnClickListener {
            binding.chipTodos.isChecked = false
            binding.chipPerdidos.isChecked = false
            binding.chipEncontrados.isChecked = true
            filtroEstado = EstadoObjeto.ENCONTRADO
            aplicarFiltros()
        }
    }

    private fun configurarBusqueda() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                textoBusqueda = newText?.trim() ?: ""
                aplicarFiltros()
                return true
            }
        })
    }

    private fun configurarBotones() {
        binding.btnReportarObjeto.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_objetos, ReportarObjetoFragment())
                .addToBackStack(null) // permite volver con el botón atrás
                .commit()
        }

        binding.navbar.setOnButtonsClickListener(
            onHome = { /* TODO */ },
            onBox = { /* ya estamos aquí */ },
            onAccess = { /* TODO */ },
            onEmergency = { /* TODO */ }
        )
    }

    private fun aplicarFiltros() {
        val resultado = listaCompleta
            .filter { filtroEstado == null || it.estado == filtroEstado }
            .filter {
                textoBusqueda.isEmpty() ||
                        it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                        it.ubicacion.contains(textoBusqueda, ignoreCase = true)
            }
            .sortedByDescending { it.fecha }
        adapter.actualizarLista(resultado)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
