package com.kathlg.flowit.data.repository

import com.kathlg.flowit.data.model.Oficina

class OficinasRepository {
    /**
     * Devuelve la lista de oficinas.
     * Aquí puedes llamar a Firebase, Room, etc.; de momento devolvemos un ejemplo estático.
     */
    fun getOficinas(): List<Oficina> {
        return listOf(
            Oficina(
                nombre = "Ofi Central",
                direccion = "C. de Beethoven, 56",
                ciudad = "Zaragoza",
                puestosTrabajo = 300,
                puestosAlumnos = 30,
                puestosTeletrabajo = 100
            ),
            Oficina(
                nombre = "Ofi Sur",
                direccion = "Av. del Sur, 123",
                ciudad = "Zaragoza",
                puestosTrabajo = 150,
                puestosAlumnos = 20,
                puestosTeletrabajo = 50
            )
            // … más oficinas si quieres
        )
    }
}
