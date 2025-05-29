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
                departamento = "Sistemas",
                tipoDocumento = "NIF",
                numeroDocumento = "12345678A",
                oficina = "OF001",
                email = "sistemas@educat.com"
            ),
            Empleado(
                nombre = "María García",
                numeroEmpleado = "EMP002",
                departamento = "Laboral",
                tipoDocumento = "NIE",
                numeroDocumento = "X1234567",
                oficina = "OF002",
                email = "laboral@educat.com"
            ),
            Empleado(
                nombre = "Userprueba",
                numeroEmpleado = "EMP002",
                departamento = "Aleatorio",
                tipoDocumento = "NIE",
                numeroDocumento = "X1234567",
                oficina = "OF002",
                email = "userprueba@educat.es"
            )
        )
    }

    /**
     * Busca un empleado por su correo.
     * Devuelve null si no encuentra ninguno.
     */
    suspend fun getEmpleadoByEmail(email: String): Empleado? = withContext(Dispatchers.IO) {
        obtenerEmpleados().firstOrNull { it.email.equals(email, ignoreCase = true) }
    }
}
