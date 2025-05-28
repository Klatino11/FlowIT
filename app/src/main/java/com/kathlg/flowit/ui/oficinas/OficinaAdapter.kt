package com.kathlg.flowit.ui.oficinas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kathlg.flowit.R
import com.kathlg.flowit.data.model.Oficina

class OficinaAdapter(
    private var listaOficinas: List<Oficina>,
    private val onItemClick: (Oficina) -> Unit
) : RecyclerView.Adapter<OficinaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OficinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_oficina, parent, false)
        return OficinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: OficinaViewHolder, position: Int) {
        val oficina = listaOficinas[position]
        holder.bind(oficina)
        holder.itemView.setOnClickListener { onItemClick(oficina) }
    }

    override fun getItemCount(): Int = listaOficinas.size

    /** Actualiza la lista de oficinas y refresca el RecyclerView */
    fun updateData(nuevaLista: List<Oficina>) {
        listaOficinas = nuevaLista
        notifyDataSetChanged()
    }
}