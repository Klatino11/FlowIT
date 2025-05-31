package com.kathlg.flowit.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kathlg.flowit.data.model.Empleado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EmpleadosRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val coleccion = firestore.collection("Empleados")

    suspend fun obtenerEmpleados(): List<Empleado> = withContext(Dispatchers.IO) {
        try {
            val snapshot = coleccion.get().await()

            snapshot.documents.mapNotNull { doc ->
                val codigo = doc.getString("Codigo")
                val nombre = doc.getString("Nombre")
                val email = doc.getString("Email")
                val tipoDocumento = doc.getString("TipoDocumento")
                val numeroDocumento = doc.getString("NumDocumento")
                val refDepto = doc.getDocumentReference("Departamento")
                val refOficina = doc.getDocumentReference("Oficina")
                val activo = doc.getBoolean("Activo") ?: false

                if (
                    codigo != null && nombre != null && email != null &&
                    tipoDocumento != null && numeroDocumento != null &&
                    refDepto != null && refOficina != null && activo
                ) {
                    Empleado(
                        id = doc.id,
                        nombre = nombre,
                        numeroEmpleado = codigo,
                        email = email,
                        tipoDocumento = tipoDocumento,
                        numeroDocumento = numeroDocumento,
                        departamento = refDepto.id,
                        oficina = refOficina.id,
                        activo = activo
                    )
                } else {
                    Log.w("EmpleadosRepository", "⚠️ Empleado inválido o inactivo: ${doc.id}")
                    null
                }
            }

        } catch (e: Exception) {
            Log.e("EmpleadosRepository", "❌ Error al obtener empleados", e)
            emptyList()
        }
    }

    suspend fun getEmpleadoByEmail(email: String): Empleado? = withContext(Dispatchers.IO) {
        obtenerEmpleados().firstOrNull { it.email.equals(email, ignoreCase = true) }
    }

    suspend fun crearEmpleado(empleado: Empleado): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val coleccion = db.collection("Empleados")

            // Obtener todos los empleados para calcular el siguiente número
            val snapshot = coleccion.get().await()

            val maxNum = snapshot.documents
                .mapNotNull { it.getString("numeroEmpleado") }
                .mapNotNull { it.removePrefix("EMPL").toIntOrNull() }
                .maxOrNull() ?: 0

            val siguienteNum = maxNum + 1
            val nuevoCodigo = "EMPL" + siguienteNum.toString().padStart(4, '0')

            // Crear copia del empleado con id y numeroEmpleado ajustados
            val empleadoFinal = empleado.copy(id = nuevoCodigo, numeroEmpleado = nuevoCodigo)

            // Guardar en Firestore usando el ID personalizado
            coleccion.document(nuevoCodigo).set(empleadoFinal).await()

            true
        } catch (e: Exception) {
            Log.e("EmpleadoRepo", "Error al crear empleado", e)
            false
        }
    }


    suspend fun desactivarEmpleado(id: String, motivo: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val updates = mutableMapOf<String, Any>("Activo" to false)
            motivo?.takeIf { it.isNotBlank() }?.let {
                updates["MotivoBaja"] = it
            }

            FirebaseFirestore.getInstance()
                .collection("Empleados")
                .document(id)
                .update(updates)
                .await()

            true
        } catch (e: Exception) {
            Log.e("EmpleadosRepository", "❌ Error al desactivar empleado $id", e)
            false
        }
    }

}
