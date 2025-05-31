package com.kathlg.flowit.ui.management.empleados

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kathlg.flowit.data.model.Empleado
import com.kathlg.flowit.data.repository.EmpleadosRepository
import kotlinx.coroutines.launch

class EmpleadosViewModel(private val repository: EmpleadosRepository) : ViewModel() {

    private val _empleados = MutableLiveData<List<Empleado>>()
    val empleados: LiveData<List<Empleado>> = _empleados

    private val _empleadosFiltrados = MutableLiveData<List<Empleado>>()
    val empleadosFiltrados: LiveData<List<Empleado>> = _empleadosFiltrados

    fun cargarEmpleados() {
        viewModelScope.launch {
            val resultado = repository.obtenerEmpleados()
            _empleados.value = resultado
            _empleadosFiltrados.value = resultado
        }
    }

    fun crearEmpleado(empleado: Empleado, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exito = repository.crearEmpleado(empleado)
            onResult(exito)
            if (exito) cargarEmpleados()
        }
    }

    fun buscarPorCodigo(query: String) {
        val empleadosActuales = _empleados.value ?: return
        val filtrados = empleadosActuales.filter {
            it.numeroEmpleado.lowercase().contains(query.trim().lowercase())
        }
        _empleadosFiltrados.value = filtrados
    }
    suspend fun desactivarEmpleado(idEmpleado: String, motivo: String): Boolean {
        return try {
            repository.desactivarEmpleado(idEmpleado, motivo)
        } catch (e: Exception) {
            false
        }
    }

}
