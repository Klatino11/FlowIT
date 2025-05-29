package com.kathlg.flowit.ui.departamentos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kathlg.flowit.data.model.Departamento
import com.kathlg.flowit.data.repository.DepartamentosRepository

class DepartamentosViewModel(
    private val repo: DepartamentosRepository
) : ViewModel() {

    private val _departamentos = MutableLiveData<List<Departamento>>()
    val departamentos: LiveData<List<Departamento>> get() = _departamentos

    /** Carga la lista de departamentos desde el repositorio */
    fun loadDepartamentos() {
        _departamentos.value = repo.getAllDepartamentos()
    }
}
