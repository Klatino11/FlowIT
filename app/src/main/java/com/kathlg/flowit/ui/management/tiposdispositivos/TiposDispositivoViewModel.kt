package com.kathlg.flowit.ui.management.tiposdispositivos

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kathlg.flowit.data.model.TipoDispositivo
import com.kathlg.flowit.data.repository.TiposDispositivosRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel que expone la lista de TipoDispositivo desde Firestore.
 */
class TiposDispositivoViewModel(
    private val repo: TiposDispositivosRepository
) : ViewModel() {

    // LiveData interno mutable
    private val _tipos = MutableLiveData<List<TipoDispositivo>>()
    // Expuesto como solo lectura
    val tipos: LiveData<List<TipoDispositivo>> = _tipos

    /**
     * Lanza la carga de tipos de dispositivos desde el repositorio.
     */
    fun cargarTiposDispositivos() {
        viewModelScope.launch {
            // Llamamos al repositorio (suspend) y publicamos la lista
            val lista = repo.obtenerTiposDispositivos()
            _tipos.value = lista
        }
    }

    fun probarConexionFirestore() {
        viewModelScope.launch {
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("TiposDispositivos")
                    .get()
                    .await()
                Log.d("FirestoreTest", "Documentos encontrados: ${snapshot.size()}")
                for (doc in snapshot.documents) {
                    Log.d("FirestoreTest", " → ${doc.id}: ${doc.data}")
                }
            } catch (e: Exception) {
                Log.e("FirestoreTest", "Error al leer Firestore: ${e.message}", e)
            }
        }
    }
}
