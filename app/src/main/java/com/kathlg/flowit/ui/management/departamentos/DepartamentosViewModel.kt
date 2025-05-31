package com.kathlg.flowit.ui.management.departamentos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kathlg.flowit.data.model.Departamento
import com.kathlg.flowit.data.repository.DepartamentosRepository
import kotlinx.coroutines.launch

class DepartamentosViewModel(
    private val repo: DepartamentosRepository
) : ViewModel() {

    private val _departamentos = MutableLiveData<List<Departamento>>()
    val departamentos: LiveData<List<Departamento>> get() = _departamentos

    private val _departamentosFiltrados = MutableLiveData<List<Departamento>>()
    val departamentosFiltrados: LiveData<List<Departamento>> get() = _departamentosFiltrados

    /** Cargar todos los departamentos desde Firestore */
    fun cargarDepartamentos() {
        viewModelScope.launch {
            val lista = repo.obtenerTodos()
            _departamentos.value = lista
            _departamentosFiltrados.value = lista
        }
    }

    /** Buscar por código (en Firestore directamente) */
    fun buscarPorCodigoFirestore(codigo: String) {
        viewModelScope.launch {
            val resultados = repo.buscarPorCodigo(codigo)
            _departamentosFiltrados.value = resultados
        }
    }
}
