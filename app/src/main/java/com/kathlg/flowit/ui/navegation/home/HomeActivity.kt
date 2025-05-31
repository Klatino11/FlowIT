package com.kathlg.flowit.ui.navegation.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigationrail.NavigationRailView
import com.google.firebase.auth.FirebaseAuth
import com.kathlg.flowit.R
import com.kathlg.flowit.SessionManager
import com.kathlg.flowit.data.repository.*
import com.kathlg.flowit.ui.authentication.login.MainActivity
import com.kathlg.flowit.ui.management.departamentos.*
import com.kathlg.flowit.ui.management.dispositivos.*
import com.kathlg.flowit.ui.management.empleados.*
import com.kathlg.flowit.ui.management.oficinas.*
import com.kathlg.flowit.ui.management.tiposdispositivos.*

class HomeActivity : AppCompatActivity() {

    private var userDialog: AlertDialog? = null

    private val tiposViewModel: TiposDispositivoViewModel by viewModels {
        TipoDispositivoViewModelFactory(TiposDispositivosRepository())
    }

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

    val empleado = SessionManager.currentEmpleado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        tiposViewModel.cargarTiposDispositivos()
        tiposViewModel.probarConexionFirestore()

        val rvListado = findViewById<RecyclerView>(R.id.rvListado).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            setHasFixedSize(true)
        }

        val navigationRail = findViewById<NavigationRailView>(R.id.navigationRail)
        val etBuscar = findViewById<EditText>(R.id.etBuscar)
        val contenedor = findViewById<FrameLayout>(R.id.flDetalles)

        val depto = empleado?.departamento?.lowercase()
        if (depto == "sistemas") {
            navigationRail.menu.findItem(R.id.nav_departamentos).isVisible = false
        } else if (depto == "laboral") {
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
                    val dispAdapter = DispositivoAdapter(emptyList()) { d ->
                        showToast("Seleccionado: ${d.nombre}")
                    }
                    rvListado.adapter = dispAdapter
                    viewModel.devices.observe(this) { dispAdapter.updateData(it) }
                    viewModel.loadDevices()
                }
                R.id.nav_empleados -> {
                    val empAdapter = EmpleadosAdapter(emptyList()) { e ->
                        showToast("Seleccionado: ${e.nombre}")
                    }
                    rvListado.adapter = empAdapter
                    empleadosViewModel.empleados.observe(this) { empAdapter.updateData(it) }
                    empleadosViewModel.cargarEmpleados()
                }
                R.id.nav_oficinas -> {
                    val ofAdapter = OficinaAdapter(emptyList()) { oficina ->
                        val detalleView = layoutInflater.inflate(R.layout.detalle_oficina, null)
                        detalleView.findViewById<TextView>(R.id.tvDetalleCodigo).text = oficina.codigo
                        detalleView.findViewById<TextView>(R.id.tvDetalleDireccion).text = oficina.direccion
                        detalleView.findViewById<TextView>(R.id.tvDetalleCiudad).text = oficina.ciudad
                        detalleView.findViewById<TextView>(R.id.tvDetallePuestosTrabajo).text =
                            "Puestos de trabajo: ${oficina.puestosTrabajo}"
                        detalleView.findViewById<TextView>(R.id.tvDetallePuestosAlumnos).text =
                            "Puestos de alumnos: ${oficina.puestosAlumnos}"
                        detalleView.findViewById<TextView>(R.id.tvDetallePuestosTeletrabajo).text =
                            "Puestos de teletrabajo: ${oficina.puestosTeletrabajo}"
                        contenedor.removeAllViews()
                        contenedor.addView(detalleView)
                    }
                    rvListado.adapter = ofAdapter
                    oficinasViewModel.oficinasFiltradas.observe(this, Observer {
                        ofAdapter.updateData(it)
                    })
                    oficinasViewModel.cargarOficinas()

                    etBuscar.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            oficinasViewModel.buscarPorCodigoFirestore(s.toString())
                        }
                    })
                }
                R.id.nav_departamentos -> {
                    val deptAdapter = DepartamentoAdapter(emptyList()) { d ->
                        // Inflar el layout de detalles
                        val detalleView = layoutInflater.inflate(R.layout.detalle_departamento, null)

                        // Llenar los campos
                        detalleView.findViewById<TextView>(R.id.tvDetalleCodigo).text = d.codigo
                        detalleView.findViewById<TextView>(R.id.tvDetalleNombre).text = d.nombre

                        // Mostrar en el contenedor de detalles
                        contenedor.removeAllViews()
                        contenedor.addView(detalleView)
                    }

                    rvListado.adapter = deptAdapter

                    departamentosViewModel.departamentosFiltrados.observe(this) {
                        deptAdapter.updateData(it)
                    }

                    departamentosViewModel.cargarDepartamentos()

                    etBuscar.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            departamentosViewModel.buscarPorCodigoFirestore(s.toString())
                        }
                    })
                }


                R.id.nav_usuario -> showUsuarioDialog()
            }
            true
        }

        val initialItemId = when (depto) {
            "laboral"  -> R.id.nav_empleados
            "sistemas" -> R.id.nav_dispositivos
            else       -> R.id.nav_dispositivos
        }
        navigationRail.selectedItemId = initialItemId
    }

    private fun showUsuarioDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_usuario, null)
        val ivAvatar        = dialogView.findViewById<ImageView>(R.id.ivAvatar)
        val tvNombreUsuario = dialogView.findViewById<TextView>(R.id.tvNombreUsuario)
        val tvCorreoUsuario = dialogView.findViewById<TextView>(R.id.tvCorreoUsuario)
        val btnVerPerfil    = dialogView.findViewById<Button>(R.id.btnVerPerfil)
        val btnCerrarSesion = dialogView.findViewById<Button>(R.id.btnCerrarSesion)

        val user = FirebaseAuth.getInstance().currentUser
        tvNombreUsuario.text = empleado?.nombre ?: "Usuario desconocido"
        tvCorreoUsuario.text = empleado?.email  ?: ""

        userDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
            .also { it.show() }

        btnVerPerfil.setOnClickListener {
            Toast.makeText(this, "Ver perfil no implementado aún", Toast.LENGTH_SHORT).show()
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
