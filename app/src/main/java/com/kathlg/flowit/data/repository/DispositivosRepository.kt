package com.kathlg.flowit.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.kathlg.flowit.data.model.Dispositivo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DispositivosRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val coleccion = firestore.collection("Dispositivos")

    /** Obtiene todos los dispositivos desde Firestore */
    suspend fun obtenerDispositivos(): List<Dispositivo> = withContext(Dispatchers.IO) {
        try {
            val snapshot = coleccion.get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    // Referencias
                    val tipoRef = doc.getDocumentReference("TipoDispositivo")
                    val asignacionRef = doc.getDocumentReference("Asignacion")

                    // Campos comunes
                    val nombre = doc.id // ID documento como nombre/código
                    val tipo = tipoRef?.id ?: "" // ID del tipo de dispositivo
                    val codigoEmpleado = asignacionRef?.id ?: ""
                    val ramGb = doc.getLong("RAM")?.toInt() ?: 0
                    val modelo = doc.getString("Modelo") ?: ""
                    val marca = doc.getString("Marca") ?: ""
                    val numeroSerie = doc.getString("NumSerie") ?: ""
                    val precio = doc.getDouble("Precio") ?: 0.0
                    val activo = doc.getBoolean("Activo") ?: true // por si se usa

                    // Para móviles
                    val numeroTelefono = doc.getString("NumTelefono")
                    val teamviewerInstalado = doc.getBoolean("TeamViewer") ?: false

                    // Para portátiles/sobremesa
                    val sistemaOperativo = doc.getString("SO")
                    val deepFreezeInstalado = doc.getBoolean("Deep Freeze") ?: false

                    Dispositivo(
                        nombre = nombre,
                        tipo = tipo,
                        codigoEmpleado = codigoEmpleado,
                        codigoOficina = "", // No se almacena en Firestore por ahora
                        ramGb = ramGb,
                        modelo = modelo,
                        marca = marca,
                        numeroSerie = numeroSerie,
                        precio = precio,
                        activo = activo,
                        numeroTelefono = numeroTelefono,
                        teamviewerInstalado = teamviewerInstalado,
                        sistemaOperativo = sistemaOperativo,
                        deepFreezeInstalado = deepFreezeInstalado
                    )
                } catch (e: Exception) {
                    Log.w("DispositivosRepository", "⚠️ Error mapeando dispositivo: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("DispositivosRepository", "❌ Error al obtener dispositivos", e)
            emptyList()
        }
    }
}
