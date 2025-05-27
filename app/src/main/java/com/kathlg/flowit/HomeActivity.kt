package com.kathlg.flowit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigationrail.NavigationRailView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val navigationRail = findViewById<NavigationRailView>(R.id.navigationRail)
        navigationRail.menu.findItem(R.id.nav_dispositivos).isChecked = false
        navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dispositivos -> showToast("Dispositivos")
                R.id.nav_empleados -> showToast("Empleados")
                R.id.nav_oficinas -> showToast("Oficinas")
                R.id.nav_departamentos -> showToast("Departamentos")
            }
            true
        }
        val navUsuario = navigationRail.menu.findItem(R.id.nav_usuario)
        navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dispositivos -> showToast("Dispositivos")
                R.id.nav_empleados -> showToast("Empleados")
                R.id.nav_oficinas -> showToast("Oficinas")
                R.id.nav_departamentos -> showToast("Departamentos")
                R.id.nav_usuario -> showUsuarioDialog() // 👈 aquí lo llamamos
            }
            true
        }


    }

    private fun showUsuarioDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_usuario, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Opcional: quitar fondo para bordes redondeados
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Manejar botones
        val btnCerrarSesion = dialogView.findViewById<Button>(R.id.btnCerrarSesion)
        val btnVerPerfil = dialogView.findViewById<Button>(R.id.btnVerPerfil)

        btnCerrarSesion.setOnClickListener {
            val confirmDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar cierre de sesión")
                .setMessage("¿Estás segura de que quieres cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .create()

            confirmDialog.setOnShowListener {
                confirmDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    ?.setTextColor(getColor(R.color.flowit_azul_oscuro))

                confirmDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    ?.setTextColor(getColor(R.color.flowit_azul_oscuro))
            }

            confirmDialog.show()
        }



        btnVerPerfil.setOnClickListener {
            Toast.makeText(this, "Mostrando detalles del empleado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
