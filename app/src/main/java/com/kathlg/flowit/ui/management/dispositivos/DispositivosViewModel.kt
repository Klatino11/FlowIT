package com.kathlg.flowit.ui.management.dispositivos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kathlg.flowit.data.model.Dispositivo
import com.kathlg.flowit.data.repository.DispositivosRepository
import kotlinx.coroutines.launch

class DispositivosViewModel(
    private val repository: DispositivosRepository
) : ViewModel()  {

    // Exponemos la lista como LiveData
    private val _devices = MutableLiveData<List<Dispositivo>>()
    val devices: LiveData<List<Dispositivo>> get() = _devices

    init {
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            val lista = repository.obtenerDispositivos()
            _devices.value = lista
        }
    }
}
