package com.kathlg.flowit.ui.management.dispositivos

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kathlg.flowit.R
import com.kathlg.flowit.data.model.Dispositivo

class DispositivoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreDispositivo)
    private val tvTipo: TextView = itemView.findViewById(R.id.tvTipoDispositivo)
    private val tvEmpleado: TextView = itemView.findViewById(R.id.tvEmpleadoAsignado)

    fun bind(dispositivo: Dispositivo, mapEmpleados: Map<String, String>) {
        tvNombre.text = dispositivo.nombre
        tvTipo.text = "Tipo: ${dispositivo.tipo}"
        // Aquí cambiamos: buscamos el nombre real
        val nombreEmpleado = mapEmpleados[dispositivo.codigoEmpleado] ?: dispositivo.codigoEmpleado
        tvEmpleado.text = "Empleado: $nombreEmpleado"
    }
}

