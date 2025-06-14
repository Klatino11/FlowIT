package com.kathlg.flowit.ui.management.empleados

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kathlg.flowit.R
import com.kathlg.flowit.data.model.Empleado

class EmpleadosAdapter(
    private var lista: List<Empleado>,
    private var mapDeptoNombres: Map<String, String>,
    private val onItemClick: (Empleado) -> Unit
) : RecyclerView.Adapter<EmpleadosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpleadosViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_empleado, parent, false)
        return EmpleadosViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmpleadosViewHolder, position: Int) {
        val item = lista[position]
        holder.bind(item, mapDeptoNombres) // Pasar el diccionario aquí
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    fun updateMapDepto(newMap: Map<String, String>) {
        Log.d("EmpleadosAdapter", "Diccionario actualizado: $newMap")
        mapDeptoNombres = newMap
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = lista.size

    /**
     * Actualiza la lista de empleados y refresca el RecyclerView.
     */
    fun updateData(nuevaLista: List<Empleado>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
