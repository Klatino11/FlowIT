package com.kathlg.flowit.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kathlg.flowit.data.model.Departamento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DepartamentosRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val coleccion = firestore.collection("Departamentos")

    /** Obtiene todos los departamentos desde Firestore */
    suspend fun obtenerTodos(): List<Departamento> = withContext(Dispatchers.IO) {
        try {
            val snapshot = coleccion.get().await()

            snapshot.documents.mapNotNull { doc ->
                val codigo = doc.getString("Codigo")
                val nombre = doc.getString("Nombre")

                if (codigo != null && nombre != null) {
                    Departamento(
                        id = doc.id,
                        codigo = codigo,
                        nombre = nombre
                    )
                } else {
                    Log.w("DepartamentosRepository", "⚠️ Documento inválido: ${doc.id}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("DepartamentosRepository", "❌ Error al obtener departamentos", e)
            emptyList()
        }
    }

    /** Búsqueda de departamentos por código */
    suspend fun buscarPorCodigo(codigo: String): List<Departamento> = withContext(Dispatchers.IO) {
        try {
            val snapshot = coleccion
                .whereGreaterThanOrEqualTo("Codigo", codigo)
                .whereLessThan("Codigo", codigo + '\uf8ff')
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val codigoDoc = doc.getString("Codigo")
                val nombre = doc.getString("Nombre")

                if (codigoDoc != null && nombre != null) {
                    Departamento(
                        id = doc.id,
                        codigo = codigoDoc,
                        nombre = nombre
                    )
                } else {
                    Log.w("DepartamentosRepository", "⚠️ Documento inválido (búsqueda): ${doc.id}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("DepartamentosRepository", "❌ Error en búsqueda por código", e)
            emptyList()
        }
    }

    suspend fun getNombreDepartamentoPorId(id: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = FirebaseFirestore.getInstance().collection("Departamentos").document(id).get().await()
            doc.getString("Nombre")
        } catch (e: Exception) {
            null
        }
    }

}
