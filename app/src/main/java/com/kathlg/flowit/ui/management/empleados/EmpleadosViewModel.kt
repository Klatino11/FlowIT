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

    private val _empleadosFiltrados = MutableLiveData<List<Empleado>>()
    val empleadosFiltrados: LiveData<List<Empleado>> = _empleadosFiltrados

    fun cargarEmpleados() {
        viewModelScope.launch {
            val resultado = repository.obtenerEmpleados()
            _empleados.value = resultado // Guarda todos los empleados para futuros filtros
            _empleadosFiltrados.value = resultado.filter { it.activo } // Solo muestra los activos por defecto
        }
    }

    fun filtrarEmpleadosAvanzado(
        codigo: String?,
        activo: Boolean?,
        oficinaId: String?,
        departamentoId: String?,
        puestosTeletrabajo: Int?,
        oficinas: List<com.kathlg.flowit.data.model.Oficina>
    ) {
        val listaOriginal = _empleados.value ?: return

        val filtrados = listaOriginal.filter { empleado ->
            val oficina = oficinas.firstOrNull { it.id == empleado.oficina }
            val cumpleTeletrabajo = puestosTeletrabajo == null || (oficina?.puestosTeletrabajo == puestosTeletrabajo)

            (codigo.isNullOrEmpty() || empleado.codigo.contains(codigo, ignoreCase = true)) &&
                    (activo == null || empleado.activo == activo) &&
                    (oficinaId.isNullOrEmpty() || empleado.oficina == oficinaId) &&
                    (departamentoId.isNullOrEmpty() || empleado.departamento == departamentoId) &&
                    cumpleTeletrabajo
        }

        _empleadosFiltrados.value = filtrados
    }



    fun crearEmpleado(empleado: Empleado, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            // 1. Obtener el siguiente código de empleado
            val siguienteCodigo = repository.obtenerSiguienteNumeroEmpleado()
            val empleadoCompleto = empleado.copy(
                id = siguienteCodigo,
                codigo = siguienteCodigo // El campo de tu modelo para código
            )
            // 2. Crear el empleado en Firestore
            val exito = repository.crearEmpleado(empleadoCompleto)
            onResult(exito)
            if (exito) cargarEmpleados()
        }
    }

    fun actualizarEmpleado(empleado: Empleado, campos: Map<String, Any>, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exito = repository.actualizarEmpleado(empleado, campos)
            callback(exito)
            if (exito) cargarEmpleados()
        }
    }



    fun buscarPorNombre(query: String) {
        val empleadosActuales = _empleados.value ?: return
        val filtrados = empleadosActuales.filter {
            it.nombre.lowercase().contains(query.trim().lowercase())
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
