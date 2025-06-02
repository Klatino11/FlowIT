package com.kathlg.flowit.ui.navegation.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigationrail.NavigationRailView
import com.google.firebase.auth.FirebaseAuth
import com.kathlg.flowit.R
import com.kathlg.flowit.SessionManager
import com.kathlg.flowit.data.repository.DepartamentosRepository
import com.kathlg.flowit.data.repository.EmpleadosRepository
import com.kathlg.flowit.data.repository.OficinasRepository
import com.kathlg.flowit.data.repository.TiposDispositivosRepository
import com.kathlg.flowit.ui.authentication.login.MainActivity
import com.kathlg.flowit.ui.management.departamentos.DepartamentosFragment
import com.kathlg.flowit.ui.management.departamentos.DepartamentosViewModel
import com.kathlg.flowit.ui.management.departamentos.DepartamentosViewModelFactory
import com.kathlg.flowit.ui.management.dispositivos.DispositivosFragment
import com.kathlg.flowit.ui.management.empleados.EmpleadoViewModelFactory
import com.kathlg.flowit.ui.management.empleados.EmpleadosFragment
import com.kathlg.flowit.ui.management.empleados.EmpleadosViewModel
import com.kathlg.flowit.ui.management.oficinas.OficinaViewModelFactory
import com.kathlg.flowit.ui.management.oficinas.OficinasFragment
import com.kathlg.flowit.ui.management.oficinas.OficinasViewModel
import com.kathlg.flowit.ui.management.tiposdispositivos.TipoDispositivoViewModelFactory
import com.kathlg.flowit.ui.management.tiposdispositivos.TiposDispositivoViewModel
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private var userDialog: AlertDialog? = null
    private var seccionActual: String = ""

    private val tiposViewModel: TiposDispositivoViewModel by viewModels {
        TipoDispositivoViewModelFactory(TiposDispositivosRepository())
    }

    val empleado = SessionManager.currentEmpleado


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        tiposViewModel.cargarTiposDispositivos()
        tiposViewModel.probarConexionFirestore()


        val navigationRail = findViewById<NavigationRailView>(R.id.navigationRail)
        val contenedor = findViewById<FrameLayout>(R.id.flDetalles)

        val depto = empleado?.departamento?.lowercase()
        if (depto == "dpt001") {
            navigationRail.menu.findItem(R.id.nav_departamentos).isVisible = false
        } else if (depto == "dpt002") {
            navigationRail.menu.findItem(R.id.nav_dispositivos).isVisible = false
        }

        tiposViewModel.tipos.observe(this) { listaTipos ->
            if (listaTipos.isNotEmpty()) {
                val prefijos = listaTipos.map { it.prefijo }
                val textoToast = prefijos.joinToString(", ")
                Toast.makeText(this, "Prefijos disponibles: $textoToast", Toast.LENGTH_LONG).show()
            }
        }


        navigationRail.setOnItemSelectedListener { item ->
            contenedor.removeAllViews()
            when (item.itemId) {
                R.id.nav_dispositivos -> {
                    seccionActual = "dispositivos"
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.flDetalles, DispositivosFragment())
                        .commit()
                }
                R.id.nav_empleados -> {
                    seccionActual = "empleados"
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.flDetalles, EmpleadosFragment())
                        .commit()
                }
                R.id.nav_departamentos -> {
                    seccionActual = "departamentos"
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.flDetalles, DepartamentosFragment())
                        .commit()
                }
                R.id.nav_oficinas -> {
                    seccionActual = "oficinas"
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.flDetalles, OficinasFragment())
                        .commit()
                }
                R.id.nav_usuario -> showUsuarioDialog()
            }
            true
        }

        val initialItemId = when (depto) {
            "dpt002"  -> R.id.nav_empleados
            "dpt001" -> R.id.nav_dispositivos
            else       -> R.id.nav_dispositivos
        }
        navigationRail.selectedItemId = initialItemId

    }

    private fun mostrarPerfilUsuario() {
        val view = layoutInflater.inflate(R.layout.dialog_perfil_usuario, null)

        val tvNombre = view.findViewById<TextView>(R.id.tvNombrePerfil)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmailPerfil)
        val tvDepto = view.findViewById<TextView>(R.id.tvDepartamentoPerfil)
        val tvOficina = view.findViewById<TextView>(R.id.tvOficinaPerfil)
        val btnCerrar = view.findViewById<Button>(R.id.btnCerrarPerfil)

        val empleado = SessionManager.currentEmpleado
        if (empleado == null) {
            showToast("Usuario no disponible")
            return
        }

        tvNombre.text = empleado.nombre
        tvEmail.text = empleado.email

        lifecycleScope.launch {
            val nombreDepto = DepartamentosRepository().getNombreDepartamentoPorId(empleado.departamento)
            val datosOfi = OficinasRepository().getDireccionCiudadPorId(empleado.oficina)

            tvDepto.text = "Departamento: ${nombreDepto ?: "Desconocido"}"
            tvOficina.text = "Oficina: ${datosOfi ?: "Desconocida"}"
        }

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(true)
            .create()

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showUsuarioDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_usuario, null)
        val tvNombreUsuario = dialogView.findViewById<TextView>(R.id.tvNombreUsuario)
        val tvCorreoUsuario = dialogView.findViewById<TextView>(R.id.tvCorreoUsuario)
        val btnVerPerfil    = dialogView.findViewById<Button>(R.id.btnVerPerfil)
        val btnCerrarSesion = dialogView.findViewById<Button>(R.id.btnCerrarSesion)

        tvNombreUsuario.text = empleado?.nombre ?: "Usuario desconocido"
        tvCorreoUsuario.text = empleado?.email  ?: ""

        userDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
            .also { it.show() }

        btnVerPerfil.setOnClickListener {
            userDialog?.dismiss()
            mostrarPerfilUsuario()
        }

        btnCerrarSesion.setOnClickListener {
            val confirmView = layoutInflater.inflate(R.layout.dialog_confirm_logout, null)
            val btnNo = confirmView.findViewById<Button>(R.id.btnNo)
            val btnSi = confirmView.findViewById<Button>(R.id.btnSi)

            val confirmDialog = AlertDialog.Builder(this)
                .setView(confirmView)
                .setCancelable(true)
                .create()
                .also { it.show() }

            btnNo.setOnClickListener { confirmDialog.dismiss() }
            btnSi.setOnClickListener {
                confirmDialog.dismiss()
                userDialog?.dismiss()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        userDialog?.dismiss()
        userDialog = null
        super.onDestroy()
    }
}
