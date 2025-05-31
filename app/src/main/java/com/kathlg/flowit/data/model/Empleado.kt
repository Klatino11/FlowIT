package com.kathlg.flowit.data.model

data class Empleado(
    val id: String = "",
    val nombre: String,
    val codigo: String,
    val departamento: String,
    val tipoDocumento: String,
    val numDocumento: String,
    val oficina: String,
    val email: String,
    val activo: Boolean = true,
    val motivoBaja: String? = null // ← nuevo campo opcional
)
