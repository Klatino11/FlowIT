package com.kathlg.flowit.ui.authentication.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kathlg.flowit.R
import com.kathlg.flowit.SessionManager
import com.kathlg.flowit.ui.authentication.auth.AuthState
import com.kathlg.flowit.ui.authentication.auth.AuthViewModel
import com.kathlg.flowit.ui.navegation.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Limpiar cualquier sesión previa
        auth = FirebaseAuth.getInstance()
        auth.signOut()
        SessionManager.currentEmpleado = null

        // 2) Inflar layout de login
        setContentView(R.layout.activity_main)

        // 3) Referencias a vistas
        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)

        // 4) Observador de estado de autenticación
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    pbLoading.visibility = View.VISIBLE
                    btnLogin.isEnabled = false
                }
                is AuthState.Success -> {
                    pbLoading.visibility = View.GONE
                    btnLogin.isEnabled = true
                    // Navegar a HomeActivity
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                is AuthState.Unauthorized -> {
                    pbLoading.visibility = View.GONE
                    btnLogin.isEnabled = true
                    Toast.makeText(
                        this,
                        "No tienes permiso para acceder a esta aplicación",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is AuthState.Error -> {
                    pbLoading.visibility = View.GONE
                    btnLogin.isEnabled = true
                    Toast.makeText(
                        this,
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // 5) Ajustar insets de sistema (opcional)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 6) Lógica del botón Login
        btnLogin.setOnClickListener {
            val email = etUsuario.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor, completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // Iniciar autenticación coordinada
            authViewModel.signIn(email, pass)
        }
    }
}
