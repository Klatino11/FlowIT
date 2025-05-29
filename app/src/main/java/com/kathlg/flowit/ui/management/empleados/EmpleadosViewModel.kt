package com.kathlg.flowit.ui.management.empleados

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kathlg.flowit.data.model.Empleado
import com.kathlg.flowit.data.repository.EmpleadosRepository
import kotlinx.coroutines.launch

class EmpleadosViewModel(
    private val repository: EmpleadosRepository
) : ViewModel() {

    private val _empleados = MutableLiveData<List<Empleado>>()
    val empleados: LiveData<List<Empleado>> = _empleados

    init {
        cargarEmpleados()
    }

    /** Lanza la carga de empleados desde el repositorio */
    fun cargarEmpleados() {
        viewModelScope.launch {
            val lista = repository.obtenerEmpleados()
            _empleados.value = lista
        }
    }
}
