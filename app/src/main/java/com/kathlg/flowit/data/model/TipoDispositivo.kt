package com.kathlg.flowit.data.model

/**
 * Representa un tipo de dispositivo:
 * - nombre: “Tablet”, “Móvil”, “Sobremesa”, “Portátil”
 * - prefijo: “TBL”, “MVL”, “DSK”, “PRT”
 */
data class TipoDispositivo(
    val id: String = "",        // Document ID; opcional pero suele venir bien
    val nombre: String = "",    // Por ejemplo: “Tablet”
    val prefijo: String = ""    // Por ejemplo: “TBL”
)
