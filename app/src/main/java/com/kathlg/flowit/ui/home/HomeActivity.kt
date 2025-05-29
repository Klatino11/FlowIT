package com.kathlg.flowit.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigationrail.NavigationRailView
import com.google.firebase.auth.FirebaseAuth
import com.kathlg.flowit.R
import com.kathlg.flowit.data.repository.DispositivosRepository
import com.kathlg.flowit.data.repository.DepartamentosRepository
import com.kathlg.flowit.data.repository.EmpleadosRepository
import com.kathlg.flowit.data.repository.OficinasRepository
import com.kathlg.flowit.ui.departamentos.DepartamentoAdapter
import com.kathlg.flowit.ui.departamentos.DepartamentosViewModel
import com.kathlg.flowit.ui.departamentos.DepartamentosViewModelFactory
import com.kathlg.flowit.ui.dispositivos.DispositivoAdapter
import com.kathlg.flowit.ui.dispositivos.DispositivosViewModel
import com.kathlg.flowit.ui.dispositivos.DispositivosViewModelFactory
import com.kathlg.flowit.ui.empleados.EmpleadoViewModelFactory
import com.kathlg.flowit.ui.empleados.EmpleadosAdapter
import com.kathlg.flowit.ui.empleados.EmpleadosViewModel
import com.kathlg.flowit.ui.login.MainActivity
import com.kathlg.flowit.ui.oficinas.OficinaAdapter
import com.kathlg.flowit.ui.oficinas.OficinasViewModel
import com.kathlg.flowit.ui.oficinas.OficinaViewModelFactory

class HomeActivity : AppCompatActivity() {

    // Mantengo la referencia para poder dismiss() más tarde
    private var userDialog: AlertDialog? = null

    private val viewModel: DispositivosViewModel by viewModels {
        DispositivosViewModelFactory(DispositivosRepository())
    }

    private val empleadosViewModel: EmpleadosViewModel by viewModels {
        EmpleadoViewModelFactory(EmpleadosRepository())
    }


    private val oficinasViewModel: OficinasViewModel by viewModels {
        OficinaViewModelFactory(OficinasRepository())
    }

    private val departamentosViewModel: DepartamentosViewModel by viewModels {
        DepartamentosViewModelFactory(DepartamentosRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val rvListado = findViewById<RecyclerView>(R.id.rvListado).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            setHasFixedSize(true)
            adapter = DispositivoAdapter(emptyList()) { dispositivo ->
                Toast.makeText(this@HomeActivity,
                    "Seleccionado: ${dispositivo.nombre}",
                    Toast.LENGTH_SHORT).show()
            }
        }

        // Observador inicial de dispositivos
        viewModel.devices.observe(this) { lista ->
            (rvListado.adapter as DispositivoAdapter).updateData(lista)
        }

        val navigationRail = findViewById<NavigationRailView>(R.id.navigationRail)
        navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dispositivos -> {
                    val dispAdapter = DispositivoAdapter(emptyList()) { d ->
                        showToast("Seleccionado: ${d.nombre}")
                    }
                    rvListado.adapter = dispAdapter
                    viewModel.devices.observe(this) { dispAdapter.updateData(it) }
                    viewModel.loadDevices()
                }
                R.id.nav_empleados -> {
                    val empAdapter = EmpleadosAdapter(emptyList()) { e ->
                        Toast.makeText(this@HomeActivity,
                            "Seleccionado: ${e.nombre}",
                            Toast.LENGTH_SHORT).show()
                    }
                    rvListado.adapter = empAdapter
                    empleadosViewModel.empleados.observe(this) { empAdapter.updateData(it) }
                    empleadosViewModel.cargarEmpleados()
                }
                R.id.nav_oficinas -> {
                    val ofAdapter = OficinaAdapter(emptyList()) { oficina ->
                        showToast("Seleccionada: ${oficina.nombre}")
                    }
                    rvListado.adapter = ofAdapter
                    oficinasViewModel.oficinas.observe(this) { ofAdapter.updateData(it) }
                    oficinasViewModel.cargarOficinas()
                }
                R.id.nav_departamentos -> {
                    val deptAdapter = DepartamentoAdapter(emptyList()) { d ->
                        showToast("Seleccionado: ${d.nombre}")
                    }
                    rvListado.adapter = deptAdapter
                    departamentosViewModel.departamentos.observe(this) {
                        deptAdapter.updateData(it)
                    }
                    departamentosViewModel.loadDepartamentos()
                }
                R.id.nav_usuario -> showUsuarioDialog()
            }
            true
        }
    }

    private fun showUsuarioDialog() {
        // 1) Infla tu layout de diálogo
        val dialogView = layoutInflater.inflate(R.layout.dialog_usuario, null)

        // 2) Obtén referencias a las vistas dentro del diálogo
        val ivAvatar        = dialogView.findViewById<ImageView>(R.id.ivAvatar)
        val tvNombreUsuario = dialogView.findViewById<TextView>(R.id.tvNombreUsuario)
        val tvCorreoUsuario = dialogView.findViewById<TextView>(R.id.tvCorreoUsuario)
        val btnVerPerfil    = dialogView.findViewById<Button>(R.id.btnVerPerfil)
        val btnCerrarSesion = dialogView.findViewById<Button>(R.id.btnCerrarSesion)

        // 3) Rellena datos del usuario actual (FirebaseAuth)
        val user = FirebaseAuth.getInstance().currentUser
        tvNombreUsuario.text = user?.displayName ?: "Usuario desconocido"
        tvCorreoUsuario.text = user?.email       ?: ""

        // 4) Crea y muestra el AlertDialog
        userDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
            .also { it.show() }

        // 5) Configura acciones de los botones
        btnVerPerfil.setOnClickListener {
            // Aquí podrías lanzar una actividad de perfil, p.ej.:
            // startActivity(Intent(this, ProfileActivity::class.java))
            Toast.makeText(this, "Ver perfil no implementado aún", Toast.LENGTH_SHORT).show()
        }

        btnCerrarSesion.setOnClickListener {
            // 1) Inflo mi layout de confirmación
            val confirmView = layoutInflater.inflate(
                R.layout.dialog_confirm_logout,
                null
            )

            // 2) Referencias
            val btnNo = confirmView.findViewById<Button>(R.id.btnNo)
            val btnSi = confirmView.findViewById<Button>(R.id.btnSi)

            // 3) Creo el AlertDialog con la vista inflada
            val confirmDialog = AlertDialog.Builder(this)
                .setView(confirmView)
                .setCancelable(true)
                .create()
                .also { it.show() }

            // 4) Acciones de los botones
            btnNo.setOnClickListener {
                confirmDialog.dismiss()
            }
            btnSi.setOnClickListener {
                // cerrar ambos diálogos
                confirmDialog.dismiss()
                userDialog?.dismiss()
                // cerrar sesión y volver al login
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
