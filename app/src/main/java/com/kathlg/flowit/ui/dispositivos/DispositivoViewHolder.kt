package com.kathlg.flowit.ui.dispositivos

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kathlg.flowit.R
import com.kathlg.flowit.data.model.Dispositivo

class DispositivoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreDispositivo)
    private val tvTipo: TextView = itemView.findViewById(R.id.tvTipoDispositivo)
    private val tvEmpleado: TextView = itemView.findViewById(R.id.tvEmpleadoAsignado)
    private val tvOficina: TextView = itemView.findViewById(R.id.tvOficina)

    fun bind(dispositivo: Dispositivo) {
        tvNombre.text = dispositivo.nombre
        tvTipo.text = "Tipo: ${dispositivo.tipo}"
        tvEmpleado.text = "Empleado: ${dispositivo.codigoEmpleado}"
        tvOficina.text = "Oficina: ${dispositivo.codigoOficina}"
    }
}
