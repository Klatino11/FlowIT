package com.kathlg.flowit.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kathlg.flowit.data.model.TipoDispositivo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TiposDispositivosRepository {

    // 1) Ahora tomamos la instancia de Firestore configurada en FlowItApp.kt:
    private val firestore = FirebaseFirestore.getInstance()
    private val coleccion = firestore.collection("TiposDispositivos")

    /**
     * Obtiene todos los documentos de la colección "TiposDispositivos"
     * y los transforma a una lista de TipoDispositivo.
     */
    suspend fun obtenerTiposDispositivos(): List<TipoDispositivo> = withContext(Dispatchers.IO) {
        try {
            // 2) Consulta “get” sobre la colección (base "flowit")
            val snapshot = coleccion.get().await()
            // 3) Mapeo de cada documento a TipoDispositivo
            snapshot.documents.mapNotNull { doc ->
                val nombre = doc.getString("Nombre")
                val prefijo = doc.getString("Prefijo")
                if (nombre != null && prefijo != null) {
                    TipoDispositivo(
                        id = doc.id,
                        nombre = nombre,
                        prefijo = prefijo
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
