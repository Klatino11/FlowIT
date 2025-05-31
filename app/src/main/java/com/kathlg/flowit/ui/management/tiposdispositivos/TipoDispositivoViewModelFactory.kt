// TipoDispositivoViewModelFactory.kt
package com.kathlg.flowit.ui.management.tiposdispositivos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kathlg.flowit.data.repository.TiposDispositivosRepository

class TipoDispositivoViewModelFactory(
    private val repo: TiposDispositivosRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TiposDispositivoViewModel::class.java)) {
            return TiposDispositivoViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
