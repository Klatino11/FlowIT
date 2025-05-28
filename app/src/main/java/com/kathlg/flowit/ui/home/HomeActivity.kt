package com.kathlg.flowit.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigationrail.NavigationRailView
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
import com.kathlg.flowit.ui.oficinas.OficinaAdapter
import com.kathlg.flowit.ui.oficinas.OficinasViewModel
import com.kathlg.flowit.ui.oficinas.OficinaViewModelFactory

class HomeActivity : AppCompatActivity() {

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
        // … tu código de diálogo existente …
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
