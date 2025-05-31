package com.kathlg.flowit.ui.management.oficinas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kathlg.flowit.data.model.Oficina
import com.kathlg.flowit.data.repository.OficinasRepository
import kotlinx.coroutines.launch

class OficinasViewModel(
    private val repo: OficinasRepository
) : ViewModel() {

    private val _oficinas = MutableLiveData<List<Oficina>>()
    val oficinas: LiveData<List<Oficina>> = _oficinas

    private val _oficinasFiltradas = MutableLiveData<List<Oficina>>()
    val oficinasFiltradas: LiveData<List<Oficina>> = _oficinasFiltradas

    fun cargarOficinas() {
        viewModelScope.launch {
            val lista = repo.obtenerOficinas()
            _oficinas.value = lista
            _oficinasFiltradas.value = lista
        }
    }

    fun buscarPorCodigoFirestore(codigo: String) {
        viewModelScope.launch {
            val resultados = repo.buscarOficinasPorCodigo(codigo)
            _oficinasFiltradas.value = resultados
        }
    }
}
