package com.kathlg.flowit.data.model

data class Oficina(
    val nombre: String,
    val direccion: String,
    val ciudad: String,
    val puestosTrabajo: Int,
    val puestosAlumnos: Int,
    val puestosTeletrabajo: Int
)