package com.kathlg.flowit.ui.management.dispositivos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kathlg.flowit.R
import com.kathlg.flowit.data.model.Dispositivo

class DispositivoAdapter(
    private var items: List<Dispositivo>,
    private var mapEmpleados: Map<String, String>, // <ID, Nombre>
    private val onItemClick: (Dispositivo) -> Unit
) : RecyclerView.Adapter<DispositivoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DispositivoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dispositivo, parent, false)
        return DispositivoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DispositivoViewHolder, position: Int) {
        val dispositivo = items[position]
        holder.bind(dispositivo, mapEmpleados)
        holder.itemView.setOnClickListener { onItemClick(dispositivo) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newList: List<Dispositivo>, newMapEmpleados: Map<String, String>) {
        this.items = newList
        this.mapEmpleados = newMapEmpleados
        notifyDataSetChanged()
    }
}

