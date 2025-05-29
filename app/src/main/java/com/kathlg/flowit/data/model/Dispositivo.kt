package com.kathlg.flowit.data.model

data class Dispositivo(
    val nombre: String,
    val tipo: String, // "Móvil", "Portátil", "Sobremesa"
    val codigoEmpleado: String,
    val codigoOficina: String,
    val ramGb: Int,
    val modelo: String,
    val marca: String,
    val numeroSerie: String,
    val precio: Double,
    val activo: Boolean = true,

    // Solo si tipo == "Móvil"
    val numeroTelefono: String? = null,
    val teamviewerInstalado: Boolean = false,

    // Solo si tipo == "Portátil" o "Sobremesa"
    val sistemaOperativo: String? = null,
    val deepFreezeInstalado: Boolean = false
)
