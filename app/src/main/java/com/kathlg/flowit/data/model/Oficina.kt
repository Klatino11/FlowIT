package com.kathlg.flowit.data.model

data class Oficina(
    val id: String,           // Ej: "OFI0001"
    val codigo: String,       // Ej: "OFI0001"
    val direccion: String,    // Ej: "C. de Ludwig van Beethoven, 56"
    val ciudad: String,       // Ej: "Zaragoza"
    val puestosTrabajo: Int,  // Ej: 300
    val puestosAlumnos: Int,  // Ej: 30
    val puestosTeletrabajo: Int // Ej: 100
)