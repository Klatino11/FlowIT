package com.kathlg.flowit.ui.management.empleados

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kathlg.flowit.R
import com.kathlg.flowit.data.model.Empleado

class EmpleadosViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvNombre: TextView = view.findViewById(R.id.tvNombreEmpleado)
    private val tvNumero: TextView = view.findViewById(R.id.tvNumEmpleado)
    private val tvDepto: TextView = view.findViewById(R.id.tvDepartamento)

    fun bind(empleado: Empleado, deptoNombres: Map<String, String>) {
        Log.d("EmpleadosViewHolder", "Empleado: ${empleado.nombre} - deptoID: ${empleado.departamento}, map: $deptoNombres")
        tvNombre.text = empleado.nombre
        tvNumero.text = "Código: ${empleado.codigo}"

        val nombreDepto = deptoNombres[empleado.departamento] ?: empleado.departamento
        tvDepto.text = "Departamento: $nombreDepto"
    }


}
