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
                        codigo = codigo,
                        email = email,
                        tipoDocumento = tipoDocumento,
                        numDocumento = numeroDocumento,
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

    suspend fun obtenerSiguienteNumeroEmpleado(): String = withContext(Dispatchers.IO) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("Empleados")
                .get()
                .await()

            val codigos = snapshot.documents.mapNotNull { it.getString("Codigo") }
            val numeros = codigos.mapNotNull { it.removePrefix("EMPL").toIntOrNull() }
            val siguiente = (numeros.maxOrNull() ?: 0) + 1
            "EMPL" + siguiente.toString().padStart(4, '0')
        } catch (e: Exception) {
            Log.e("EmpleadosRepository", "❌ Error obteniendo siguiente código", e)
            "EMPL0001"
        }
    }

    suspend fun crearEmpleado(empleado: Empleado): Boolean = withContext(Dispatchers.IO) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val docRefDepto = firestore.document("Departamentos/${empleado.departamento}")
            val docRefOfi = firestore.document("Oficinas/${empleado.oficina}")

            // Construimos el mapa de campos tal y como los espera Firestore
            val data = mutableMapOf<String, Any>(
                "Nombre" to empleado.nombre,
                "Codigo" to empleado.codigo,   // asegúrate de usar el campo correcto
                "TipoDocumento" to empleado.tipoDocumento,
                "NumDocumento" to empleado.numDocumento,
                "Email" to empleado.email,
                "Departamento" to docRefDepto,
                "Oficina" to docRefOfi,
                "Activo" to true
            )

            // Si motivoBaja es nulo o vacío, no lo incluimos en la creación
            if (!empleado.motivoBaja.isNullOrEmpty()) {
                data["MotivoBaja"] = empleado.motivoBaja
            }

            firestore.collection("Empleados")
                .document(empleado.id)
                .set(data)
                .await()
            true
        } catch (e: Exception) {
            Log.e("EmpleadosRepository", "❌ Error al crear empleado", e)
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
