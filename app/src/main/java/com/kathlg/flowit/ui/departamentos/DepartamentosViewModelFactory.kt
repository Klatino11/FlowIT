package com.kathlg.flowit.ui.departamentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kathlg.flowit.data.repository.DepartamentosRepository

class DepartamentosViewModelFactory(
    private val repo: DepartamentosRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DepartamentosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DepartamentosViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
