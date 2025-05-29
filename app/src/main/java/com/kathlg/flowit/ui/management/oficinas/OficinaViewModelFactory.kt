package com.kathlg.flowit.ui.management.oficinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kathlg.flowit.data.repository.OficinasRepository

class OficinaViewModelFactory(
    private val repo: OficinasRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OficinasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OficinasViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
