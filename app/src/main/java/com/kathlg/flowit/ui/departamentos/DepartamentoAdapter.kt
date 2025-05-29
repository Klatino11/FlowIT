package com.kathlg.flowit.ui.departamentos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kathlg.flowit.R
import com.kathlg.flowit.data.model.Departamento

class DepartamentoAdapter(
    private var items: List<Departamento>,
    private val onItemClick: (Departamento) -> Unit
) : RecyclerView.Adapter<DepartamentoViewHolder>() {

    fun updateData(newList: List<Departamento>) {
        this.items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_departamento, parent, false)
        return DepartamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DepartamentoViewHolder, position: Int) {
        val depto = items[position]
        holder.bind(depto)
        holder.itemView.setOnClickListener { onItemClick(depto) }
    }

    override fun getItemCount(): Int = items.size
}

