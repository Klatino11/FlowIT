package com.kathlg.flowit.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kathlg.flowit.data.model.Oficina
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OficinasRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val coleccion = firestore.collection("Oficinas")

    suspend fun obtenerOficinas(): List<Oficina> = withContext(Dispatchers.IO) {
        try {
            val snapshot = coleccion.get().await()

            snapshot.documents.mapNotNull { doc ->
                val codigo = doc.getString("Codigo")
                val direccion = doc.getString("Direccion")
                val ciudad = doc.getString("Ciudad")
                val puestosTrabajoLong = doc.getLong("PuestosTrabajo")
                val puestosAlumnosLong = doc.getLong("PuestosAlumnos")
                val puestosTeletrabajoLong = doc.getLong("PuestosTeletrabajo")

                if (
                    codigo != null &&
                    direccion != null &&
                    ciudad != null &&
                    puestosTrabajoLong != null &&
                    puestosAlumnosLong != null &&
                    puestosTeletrabajoLong != null
                ) {
                    Oficina(
                        id = doc.id,
                        codigo = codigo,
                        direccion = direccion,
                        ciudad = ciudad,
                        puestosTrabajo = puestosTrabajoLong.toInt(),
                        puestosAlumnos = puestosAlumnosLong.toInt(),
                        puestosTeletrabajo = puestosTeletrabajoLong.toInt()
                    )
                } else {
                    Log.w("OficinasRepository", "⚠️ Documento con campos nulos: ${doc.id}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("OficinasRepository", "❌ Error al obtener oficinas", e)
            emptyList()
        }
    }



}
