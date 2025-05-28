package com.kathlg.flowit.data.repository

import com.kathlg.flowit.data.model.Empleado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmpleadosRepository {

    /**
     * Simula la carga de empleados desde un origen de datos (p.ej. Firestore).
     * Más adelante cambiaremos el stub por la llamada real.
     */
    suspend fun obtenerEmpleados(): List<Empleado> = withContext(Dispatchers.IO) {
        listOf(
            Empleado(
                nombre = "Juan Pérez",
                numeroEmpleado = "EMP001",
                departamento = "Ventas",
                tipoDocumento = "DNI",
                numeroDocumento = "12345678A",
                oficina = "OF001"
            ),
            Empleado(
                nombre = "María García",
                numeroEmpleado = "EMP002",
                departamento = "Marketing",
                tipoDocumento = "Pasaporte",
                numeroDocumento = "X1234567",
                oficina = "OF002"
            )
        )
    }
}
