package com.kathlg.flowit.data.repository

import com.kathlg.flowit.data.model.Dispositivo

class DispositivosRepository {

    // Por ahora devolvemos lista fija; luego conectarás con Firestore
    fun getAllDispositivos(): List<Dispositivo> {
        return listOf(
            Dispositivo(
                nombre = "MVL0001",
                tipo = "Móvil",
                codigoEmpleado = "EMP0001",
                codigoOficina = "OF0001",
                ramGb = 4,
                modelo = "Galaxy A12",
                marca = "Samsung",
                numeroSerie = "SN123456",
                precio = 199.99,
                numeroTelefono = "600123456",
                teamviewerInstalado = true
            ),
            Dispositivo(
                nombre = "PRT0001",
                tipo = "Portátil",
                codigoEmpleado = "EMP0003",
                codigoOficina = "OF0001",
                ramGb = 8,
                modelo = "ThinkPad L14",
                marca = "Lenovo",
                numeroSerie = "SN654321",
                precio = 849.99,
                sistemaOperativo = "Windows 11",
                deepFreezeInstalado = false
            )
        )
    }
}