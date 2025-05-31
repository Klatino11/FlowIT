package com.kathlg.flowit.ui.management.departamentos

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kathlg.flowit.R
import com.kathlg.flowit.data.model.Departamento

class DepartamentoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvCodigo: TextView = view.findViewById(R.id.tvCodigoDepartamento)
    private val tvNombre: TextView = view.findViewById(R.id.tvNombreDepartamento)

    fun bind(depto: Departamento) {
        tvCodigo.text = "${depto.codigo}"
        tvNombre.text = "Nombre: ${depto.nombre}"
    }
}
