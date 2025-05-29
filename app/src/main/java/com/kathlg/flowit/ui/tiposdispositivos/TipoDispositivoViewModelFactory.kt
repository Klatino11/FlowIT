package com.kathlg.flowit.ui.tiposdispositivos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kathlg.flowit.data.repository.OficinasRepository

class TipoDispositivoViewModelFactory(
    private val repo: OficinasRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TiposDispositivoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TiposDispositivoViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
