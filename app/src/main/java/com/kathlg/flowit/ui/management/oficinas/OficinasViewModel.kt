package com.kathlg.flowit.ui.management.oficinas

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kathlg.flowit.data.model.Oficina
import com.kathlg.flowit.data.repository.OficinasRepository
import kotlinx.coroutines.launch

class OficinasViewModel(
    private val repo: OficinasRepository
) : ViewModel() {

    private val _oficinas = MutableLiveData<List<Oficina>>()
    val oficinas: LiveData<List<Oficina>> = _oficinas

    fun cargarOficinas() {
        viewModelScope.launch {
            val lista = repo.obtenerOficinas()
            Log.d("OficinasViewModel", "🔍 Oficinas recibidas: ${lista.size}")
            lista.forEach {
                Log.d("OficinasViewModel", "👉 ${it.codigo} - ${it.direccion}")
            }
            _oficinas.value = lista
        }
    }
}
