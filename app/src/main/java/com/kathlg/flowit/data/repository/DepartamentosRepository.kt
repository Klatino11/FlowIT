package com.kathlg.flowit.data.repository

import com.kathlg.flowit.data.model.Departamento

/**
 * Provee acceso a los datos de Departamentos.
 * Por ahora usamos una lista fija; más adelante conectaremos a Firestore.
 */
class DepartamentosRepository {

    /** Devuelve todos los departamentos disponibles */
    fun getAllDepartamentos(): List<Departamento> {
        // TODO: sustituir por llamada a Firestore
        return listOf(
            Departamento(codigo = "DEP001", nombre = "Recursos Humanos"),
            Departamento(codigo = "DEP002", nombre = "IT"),
            Departamento(codigo = "DEP003", nombre = "Ventas")
        )
    }
}
