package com.kathlg.flowit.ui.management.dispositivos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kathlg.flowit.data.repository.DispositivosRepository

class DispositivosViewModelFactory(
    private val repo: DispositivosRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DispositivosViewModel(repo) as T
    }
}