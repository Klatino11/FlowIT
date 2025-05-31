package com.kathlg.flowit.ui.management.empleados

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kathlg.flowit.data.repository.EmpleadosRepository

class EmpleadoViewModelFactory(private val repository: EmpleadosRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmpleadosViewModel::class.java)) {
            return EmpleadosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
