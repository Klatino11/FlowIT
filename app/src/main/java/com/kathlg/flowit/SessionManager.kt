package com.kathlg.flowit

import com.kathlg.flowit.data.model.Empleado

/**
 * Guarda en memoria el empleado logueado para que
 * cualquier parte de la app pueda consultarlo.
 */
object SessionManager {
    var currentEmpleado: Empleado? = null
}
