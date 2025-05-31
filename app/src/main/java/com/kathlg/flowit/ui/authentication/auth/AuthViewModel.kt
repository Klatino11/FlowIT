package com.kathlg.flowit.ui.authentication.auth

import com.kathlg.flowit.data.model.Empleado
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kathlg.flowit.SessionManager
import com.kathlg.flowit.data.repository.EmpleadosRepository
import kotlinx.coroutines.launch

/**
 * Estados posibles tras intentar autenticarse:
 * - Loading: estamos en proceso
 * - Success: login + permiso OK; lleva el Empleado
 * - Unauthorized: login OK pero depto no permitido
 * - Error: fallo de login o de red; lleva mensaje
 */
sealed class AuthState {
    object Loading : AuthState()
    data class Success(val empleado: Empleado) : AuthState()
    object Unauthorized : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repo: EmpleadosRepository = EmpleadosRepository(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    /**
     * Intenta autenticar con Firebase, luego comprueba en EmpleadosRepository
     * y valida el departamento.
     */
    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Autenticación Firebase exitosa, buscar el empleado en Firestore
                viewModelScope.launch {
                    val emp = repo.getEmpleadoByEmail(email)

                    if (emp == null) {
                        _authState.postValue(AuthState.Error("Correo no registrado en la empresa"))
                    } else if (!emp.activo) {
                        firebaseAuth.signOut()
                        _authState.postValue(AuthState.Error("Usuario desactivado. Contacta con administración."))
                    } else {
                        Log.d("AuthDebug", "Depto: '${emp.departamento}' (activo: ${emp.activo})")

                        val depto = emp.departamento.lowercase()
                        if (depto == "dpt001" || depto == "dpt002") {
                            SessionManager.currentEmpleado = emp
                            _authState.postValue(AuthState.Success(emp))
                        } else {
                            firebaseAuth.signOut()
                            _authState.postValue(AuthState.Unauthorized)
                        }
                    }
                }
            }
            .addOnFailureListener { ex ->
                _authState.value = AuthState.Error(ex.message ?: "Error de autenticación")
            }
    }

}