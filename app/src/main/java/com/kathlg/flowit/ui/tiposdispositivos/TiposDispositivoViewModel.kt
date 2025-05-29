package com.kathlg.flowit.ui.tiposdispositivos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kathlg.flowit.data.model.Oficina
import com.kathlg.flowit.data.repository.OficinasRepository

class TiposDispositivoViewModel(
    private val repo: OficinasRepository
) : ViewModel() {

    // Backing LiveData mutable
    private val _oficinas = MutableLiveData<List<Oficina>>()
    // Expuesto como sólo lectura
    val oficinas: LiveData<List<Oficina>> = _oficinas

    /**
     * Carga las oficinas desde el repositorio y publica en el LiveData.
     */
    fun cargarOficinas() {
        val lista = repo.getOficinas()
        _oficinas.value = lista
    }
}
