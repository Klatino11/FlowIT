package com.kathlg.flowit.ui.management.oficinas

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kathlg.flowit.R
import com.kathlg.flowit.data.model.Oficina

class OficinaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvNombre: TextView = view.findViewById(R.id.tvNombreOficina)
    private val tvDireccion: TextView = view.findViewById(R.id.tvDireccionOficina)
    private val tvCiudad: TextView = view.findViewById(R.id.tvCiudadOficina)

    fun bind(oficina: Oficina) {
        tvNombre.text = oficina.codigo
        tvDireccion.text = "Dirección: ${oficina.direccion}"
        tvCiudad.text = "Ciudad: ${oficina.ciudad}"
    }
}