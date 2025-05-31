package com.kathlg.flowit.ui.navegation.home

import com.kathlg.flowit.data.model.Empleado
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private var userDialog: AlertDialog? = null
    private var seccionActual: String = ""

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
        val fabExportar = findViewById<FloatingActionButton>(R.id.fabExportarCSV)

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

        val fabCrear = findViewById<FloatingActionButton>(R.id.fabCrearRegistro).setOnClickListener {
            val view = layoutInflater.inflate(R.layout.dialog_nuevo_empleado, null)
            val etNombre = view.findViewById<EditText>(R.id.etNombreEmpleado)
            val etNumeroDoc = view.findViewById<EditText>(R.id.etNumeroDocumento)
            val spDepartamento = view.findViewById<Spinner>(R.id.spDepartamento)
            val spTipoDoc = view.findViewById<Spinner>(R.id.spTipoDocumento)
            val spOficina = view.findViewById<Spinner>(R.id.spOficina)
            val btnCrear = view.findViewById<Button>(R.id.btnCrearNuevo)
            val btnCancelar = view.findViewById<Button>(R.id.btnCancelarNuevo)

            val dialog = AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create()

            // Cargar departamentos y oficinas desde el ViewModel
            lifecycleScope.launch {
                val departamentos = departamentosViewModel.departamentosFiltrados.value ?: emptyList()
                val oficinas = oficinasViewModel.oficinasFiltradas.value ?: emptyList()

                spDepartamento.adapter = ArrayAdapter(this@HomeActivity, android.R.layout.simple_spinner_dropdown_item, departamentos.map { it.nombre })
                spOficina.adapter = ArrayAdapter(this@HomeActivity, android.R.layout.simple_spinner_dropdown_item, oficinas.map { it.codigo })
                spTipoDoc.adapter = ArrayAdapter(this@HomeActivity, android.R.layout.simple_spinner_dropdown_item, listOf("NIF", "NIE"))
            }

            btnCancelar.setOnClickListener {
                dialog.dismiss()
            }

            btnCrear.setOnClickListener {
                val nombre = etNombre.text.toString().trim()
                val docNum = etNumeroDoc.text.toString().trim()
                val tipoDoc = spTipoDoc.selectedItem?.toString()?.trim() ?: ""
                val deptoNombre = spDepartamento.selectedItem?.toString()?.trim() ?: ""
                val oficinaCodigo = spOficina.selectedItem?.toString()?.trim() ?: ""

                if (nombre.isEmpty() || docNum.isEmpty() || deptoNombre.isEmpty() || oficinaCodigo.isEmpty()) {
                    showToast("Completa todos los campos.")
                    return@setOnClickListener
                }

                // Confirmación antes de crear
                mostrarConfirmacionCreacion(nombre) {
                    // Obtener el ID real del departamento y oficina seleccionados
                    val departamentoId = departamentosViewModel.departamentosFiltrados.value?.firstOrNull { it.nombre == deptoNombre }?.id ?: ""
                    val oficinaId = oficinasViewModel.oficinasFiltradas.value?.firstOrNull { it.codigo == oficinaCodigo }?.id ?: ""

                    val nuevoEmpleado = Empleado(
                        id = "",
                        nombre = nombre,
                        numeroEmpleado = "", // se calcula en el repo
                        tipoDocumento = tipoDoc,
                        numeroDocumento = docNum,
                        email = "",
                        departamento = departamentoId,
                        oficina = oficinaId,
                        activo = true,
                        motivoBaja = ""
                    )

                    empleadosViewModel.crearEmpleado(nuevoEmpleado) { exito ->
                        if (exito) {
                            showToast("Empleado creado con éxito")
                            empleadosViewModel.cargarEmpleados()
                        } else {
                            showToast("Error al crear empleado")
                        }
                    }

                    dialog.dismiss()
                }
            }

            dialog.show()
        }

        fabExportar.setOnClickListener {
            val builder = StringBuilder()
            val fileName: String
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())

            when (seccionActual) {
                "departamentos" -> {
                    val data = departamentosViewModel.departamentosFiltrados.value
                    if (data.isNullOrEmpty()) {
                        showToast("No hay departamentos para exportar.")
                        return@setOnClickListener
                    }
                    builder.append("ID,Codigo,Nombre\n")
                    data.forEach {
                        builder.append("${it.id},${it.codigo},${it.nombre}\n")
                    }
                }
                "oficinas" -> {
                    val data = oficinasViewModel.oficinasFiltradas.value
                    if (data.isNullOrEmpty()) {
                        showToast("No hay oficinas para exportar.")
                        return@setOnClickListener
                    }
                    builder.append("ID,Codigo,Direccion,Ciudad,PuestosTrabajo,PuestosAlumnos,PuestosTeletrabajo\n")
                    data.forEach {
                        builder.append("${it.id},${it.codigo},${it.direccion},${it.ciudad},${it.puestosTrabajo},${it.puestosAlumnos},${it.puestosTeletrabajo}\n")
                    }
                }
                "empleados" -> {
                    val data = empleadosViewModel.empleadosFiltrados.value
                    if (data.isNullOrEmpty()) {
                        showToast("No hay empleados para exportar.")
                        return@setOnClickListener
                    }
                    builder.append("ID,Codigo,Nombre,Email,TipoDocumento,NumDocumento,Departamento,Oficina\n")
                    data.forEach {
                        builder.append("${it.id},${it.numeroEmpleado},${it.nombre},${it.email},${it.tipoDocumento},${it.numeroDocumento},${it.departamento},${it.oficina}\n")
                    }
                }
                else -> {
                    showToast("Selecciona una sección primero.")
                    return@setOnClickListener
                }
            }

            fileName = "${seccionActual.replaceFirstChar { it.uppercase() }}_Export_$timestamp.csv"

            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                file.writeText(builder.toString())
                showToast("CSV exportado en: ${file.absolutePath}")
            } catch (e: Exception) {
                showToast("Error al exportar CSV")
                e.printStackTrace()
            }
        }

        navigationRail.setOnItemSelectedListener { item ->
            contenedor.removeAllViews()
            when (item.itemId) {
                R.id.nav_dispositivos -> {
                    seccionActual = "dispositivos"
                    val dispAdapter = DispositivoAdapter(emptyList()) { d ->
                        showToast("Seleccionado: ${d.nombre}")
                    }
                    rvListado.adapter = dispAdapter
                    viewModel.devices.observe(this) { dispAdapter.updateData(it) }
                    viewModel.loadDevices()
                }
                R.id.nav_empleados -> {
                    seccionActual = "empleados"
                    val empAdapter = EmpleadosAdapter(emptyList()) { empleado ->
                        val detalleView = layoutInflater.inflate(R.layout.detalle_empleado, null)



                        detalleView.findViewById<TextView>(R.id.tvDetalleNombre).text = empleado.nombre
                        detalleView.findViewById<TextView>(R.id.tvDetalleEmail).text = empleado.email
                        detalleView.findViewById<TextView>(R.id.tvDetalleDocumento).text =
                            "${empleado.tipoDocumento}: ${empleado.numeroDocumento}"

                        detalleView.findViewById<ImageView>(R.id.btnEditarEmpleado).setOnClickListener {
                            showToast("Editar empleado")
                        }

                        detalleView.findViewById<ImageView>(R.id.btnDesactivarEmpleado).setOnClickListener {
                            val dialogView = layoutInflater.inflate(R.layout.dialog_desactivar, null)

                            val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTituloDesactivar)
                            val etMotivo = dialogView.findViewById<EditText>(R.id.etMotivoBaja)
                            val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarDesactivar)
                            val btnDesactivar = dialogView.findViewById<Button>(R.id.btnConfirmarDesactivar)

                            tvTitulo.text = "${empleado.nombre} (Desactivar)"

                            val alertDialog = AlertDialog.Builder(this)
                                .setView(dialogView)
                                .setCancelable(true)
                                .create()

                            btnCancelar.setOnClickListener {
                                alertDialog.dismiss()
                            }

                            btnDesactivar.setOnClickListener {
                                val motivo = etMotivo.text.toString().trim()
                                if (motivo.isEmpty()) {
                                    showToast("Por favor, introduce un motivo")
                                } else {
                                    alertDialog.dismiss()
                                    mostrarDialogoDesactivarEmpleado(empleado.copy(), motivo) // ← llamado correcto
                                }
                            }

                            alertDialog.show()
                        }


                        val deptRepo = DepartamentosRepository()
                        val ofiRepo = OficinasRepository()

                        lifecycleScope.launch {
                            val nombreDepto = deptRepo.getNombreDepartamentoPorId(empleado.departamento)
                            val datosOfi = ofiRepo.getDireccionCiudadPorId(empleado.oficina)

                            detalleView.findViewById<TextView>(R.id.tvDetalleDepartamento).text =
                                "Departamento: ${nombreDepto ?: "Desconocido"}"
                            detalleView.findViewById<TextView>(R.id.tvDetalleOficina).text =
                                "Oficina: ${datosOfi ?: "Desconocida"}"
                        }

                        contenedor.removeAllViews()
                        contenedor.addView(detalleView)
                    }
                    rvListado.adapter = empAdapter
                    empleadosViewModel.empleadosFiltrados.observe(this) {
                        empAdapter.updateData(it)
                    }
                    empleadosViewModel.cargarEmpleados()
                    etBuscar.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            empleadosViewModel.buscarPorCodigo(s.toString())
                        }
                    })
                }
                R.id.nav_oficinas -> {
                    seccionActual = "oficinas"
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
                    seccionActual = "departamentos"
                    val deptAdapter = DepartamentoAdapter(emptyList()) { d ->
                        val detalleView = layoutInflater.inflate(R.layout.detalle_departamento, null)
                        detalleView.findViewById<TextView>(R.id.tvDetalleCodigo).text = d.codigo
                        detalleView.findViewById<TextView>(R.id.tvDetalleNombre).text = d.nombre
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


    private fun mostrarConfirmacionCreacion(nombre: String, onConfirm: () -> Unit) {
        val confirmView = layoutInflater.inflate(R.layout.dialog_confirm, null)
        val tvMensaje = confirmView.findViewById<TextView>(R.id.tvConfirmMessage)
        val btnCancelar = confirmView.findViewById<Button>(R.id.btnCancelar)
        val btnConfirmar = confirmView.findViewById<Button>(R.id.btnConfirmar)

        tvMensaje.text = "¿Deseas crear el empleado \"$nombre\"?"

        val confirmDialog = AlertDialog.Builder(this)
            .setView(confirmView)
            .setCancelable(true)
            .create()

        btnCancelar.setOnClickListener { confirmDialog.dismiss() }
        btnConfirmar.setOnClickListener {
            confirmDialog.dismiss()
            onConfirm()
        }

        confirmDialog.show()
    }


    private fun mostrarDialogoDesactivarEmpleado(empleado: Empleado, motivo:String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm, null)
        val tvMensaje     = dialogView.findViewById<TextView>(R.id.tvConfirmMessage)
        val etMotivo      = dialogView.findViewById<TextView>(R.id.etRegistro)
        val btnCancelar   = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnConfirmar  = dialogView.findViewById<Button>(R.id.btnConfirmar)

        // Mensaje personalizado (opcional)
        tvMensaje.text = "¿Seguro que deseas desactivar a ${empleado.nombre}?"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            if (motivo.isEmpty()) {
                Toast.makeText(this, "Motivo de baja no proporcionado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val exito = empleadosViewModel.desactivarEmpleado(empleado.id, motivo)
                if (exito) {
                    Toast.makeText(this@HomeActivity, "Empleado desactivado", Toast.LENGTH_SHORT).show()
                    empleadosViewModel.cargarEmpleados()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@HomeActivity, "Error al desactivar", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }


    private fun showUsuarioDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_usuario, null)
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
