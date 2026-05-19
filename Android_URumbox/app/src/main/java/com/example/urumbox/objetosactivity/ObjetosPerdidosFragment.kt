package com.example.urumbox.objetosactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urumbox.ui.objetosperdidos.ObjetoViewModel
import com.example.urumbox.R
import com.example.urumbox.objetosactivity.ReportarObjetoFragment
import com.example.urumbox.data.model.objetosperdidos.EstadoObjeto
import com.example.urumbox.data.model.objetosperdidos.ObjetoPerdido
import com.example.urumbox.databinding.FragmentObjetosPerdidosBinding

class ObjetosPerdidosFragment : Fragment() {

    private var _binding: FragmentObjetosPerdidosBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ObjetosAdapter
    private lateinit var viewModel: ObjetoViewModel

    private var filtroEstado: EstadoObjeto? = null
    private var textoBusqueda: String = ""
    private var listaCompleta: List<ObjetoPerdido> = emptyList()

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

        configurarViewModel()
        configurarRecyclerView()
        configurarFiltros()
        configurarBusqueda()
        configurarBotones()

        // ── CONSULTA: carga objetos desde Firestore al abrir la pantalla ──
        viewModel.cargarObjetos()
    }

    private fun configurarViewModel() {
        viewModel = ViewModelProvider(this)[ObjetoViewModel::class.java]

        // Observa la lista de objetos
        viewModel.objetos.observe(viewLifecycleOwner) { lista ->
            listaCompleta = lista
            aplicarFiltros()
        }

        // Observa el estado de carga
        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        // Observa mensajes de éxito o error
        viewModel.mensaje.observe(viewLifecycleOwner) { mensaje ->
            if (mensaje.isNotEmpty()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            }
        }
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
                .addToBackStack(null)
                .commit()
        }
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