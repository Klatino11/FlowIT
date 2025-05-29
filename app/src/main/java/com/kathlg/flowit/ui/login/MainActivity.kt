package com.kathlg.flowit.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kathlg.flowit.R
import com.kathlg.flowit.ui.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.kathlg.flowit.SessionManager
import com.kathlg.flowit.ui.auth.AuthState
import com.kathlg.flowit.ui.auth.AuthViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        FirebaseAuth.getInstance().signOut()
        SessionManager.currentEmpleado = null
        setContentView(R.layout.activity_main)

        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    // Opcional: muestra un ProgressBar
                }
                is AuthState.Success -> {
                    // Login + permiso OK: abre Home pasando el empleado si lo necesitas
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                is AuthState.Unauthorized -> {
                    Toast.makeText(this,
                        "No tienes permiso para acceder a esta aplicación",
                        Toast.LENGTH_LONG).show()
                }
                is AuthState.Error -> {
                    Toast.makeText(this,
                        state.message,
                        Toast.LENGTH_LONG).show()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etUsuario.text.toString().trim()
            val pass  = etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Lanza la autenticación coordinada
            authViewModel.signIn(email, pass)
        }

    }
}
