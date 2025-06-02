package com.kathlg.flowit.ui.management.empleados

import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.kathlg.flowit.R
import com.kathlg.flowit.SessionManager
import com.kathlg.flowit.data.model.Departamento
import com.kathlg.flowit.data.model.Empleado
import com.kathlg.flowit.data.model.Oficina
import com.kathlg.flowit.data.repository.DepartamentosRepository
import com.kathlg.flowit.data.repository.EmpleadosRepository
import com.kathlg.flowit.data.repository.OficinasRepository
import com.kathlg.flowit.ui.management.departamentos.DepartamentosViewModel
import com.kathlg.flowit.ui.management.departamentos.DepartamentosViewModelFactory
import com.kathlg.flowit.ui.management.oficinas.OficinaViewModelFactory
import com.kathlg.flowit.ui.management.oficinas.OficinasViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmpleadosFragment : Fragment() {

    private val empleadosViewModel: EmpleadosViewModel by activityViewModels {
        EmpleadoViewModelFactory(EmpleadosRepository())
    }

    private val departamentosViewModel: DepartamentosViewModel by activityViewModels {
        DepartamentosViewModelFactory(DepartamentosRepository())
    }

    private val oficinasViewModel: OficinasViewModel by activityViewModels {
        OficinaViewModelFactory(OficinasRepository())
    }

    private var mapDeptoNombres: Map<String, String> = emptyMap()
    private var empAdapter: EmpleadosAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_empleados, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rvListado = view.findViewById<RecyclerView>(R.id.rvListado).apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        val etBuscar = view.findViewById<EditText>(R.id.etBuscar)
        val contenedor = view.findViewById<FrameLayout>(R.id.flDetalles)

        // Escucha departamentos para asociar nombres
        departamentosViewModel.departamentos.observe(viewLifecycleOwner) { listaDeptos ->
            mapDeptoNombres = listaDeptos.associate { it.id to it.nombre }
            empAdapter?.updateMapDepto(mapDeptoNombres)
            empAdapter?.updateData(empleadosViewModel.empleadosFiltrados.value ?: emptyList())
        }

        // Adapter y listener
        empAdapter = EmpleadosAdapter(
            emptyList(),
            mapDeptoNombres
        ) { empleado -> mostrarDetalleEmpleado(empleado, contenedor) }
        rvListado.adapter = empAdapter

        // Escucha cambios en empleados filtrados
        empleadosViewModel.empleadosFiltrados.observe(viewLifecycleOwner) {
            empAdapter?.updateData(it)
        }

        val fabExportar = view.findViewById<FloatingActionButton>(R.id.fabExportarCSV)
        fabExportar.setOnClickListener {
            exportarEmpleadosCSV()
        }

        val fabCrear = view.findViewById<FloatingActionButton>(R.id.fabCrearRegistro)
        fabCrear.setOnClickListener {
            // Carga los datos necesarios antes de mostrar el diálogo
            lifecycleScope.launch {
                val listaDeptos = departamentosViewModel.departamentos.value ?: emptyList()
                val listaOfis = oficinasViewModel.oficinasFiltradas.value ?: emptyList()
                if (listaDeptos.isEmpty() || listaOfis.isEmpty()) {
                    departamentosViewModel.cargarDepartamentos()
                    oficinasViewModel.cargarOficinas()
                    showToast("Cargando datos, por favor intenta de nuevo en unos segundos")
                    return@launch
                }
                mostrarDialogoCrearEmpleado(listaDeptos, listaOfis)
            }
        }
        empleadosViewModel.cargarEmpleados()

        // Búsqueda
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                empleadosViewModel.buscarPorCodigo(s.toString())
            }
        })
    }

    /*** === EDITAR EMPLEADO === ***/
    private fun mostrarDialogoEditarEmpleado(
        empleado: Empleado,
        listaDeptos: List<Departamento>,
        rol: String
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_empleado, null)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmailEmpleado)
        val spDepartamento = dialogView.findViewById<Spinner>(R.id.spDepartamentoEditar)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarEdicion)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarEdicion)

        // Prellenar campos
        etEmail.setText(empleado.email)

        // Adaptador personalizado para Departamento (rellena el spinner)
        val adapterDepto = object : ArrayAdapter<Departamento>(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, listaDeptos
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).text = getItem(position)?.nombre ?: ""
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).text = getItem(position)?.nombre ?: ""
                return view
            }
        }
        spDepartamento.adapter = adapterDepto
        val indexDeptoActual = listaDeptos.indexOfFirst { it.id == empleado.departamento }
        if (indexDeptoActual >= 0) spDepartamento.setSelection(indexDeptoActual)

        // Lógica de permisos:
        when (rol) {
            "dpt001" -> {
                etEmail.isEnabled = true
                spDepartamento.isEnabled = false
            }
            "dpt002" -> {
                etEmail.isEnabled = false
                spDepartamento.isEnabled = true
            }
            else -> {
                etEmail.isEnabled = false
                spDepartamento.isEnabled = false
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val campos = mutableMapOf<String, Any>()
            if (rol == "dpt001") {
                val nuevoEmail = etEmail.text.toString().trim()
                if (nuevoEmail != empleado.email) {
                    campos["Email"] = nuevoEmail
                }
            }
            if (rol == "dpt002") {
                val nuevoDepto = spDepartamento.selectedItem as Departamento
                if (nuevoDepto.id != empleado.departamento) {
                    val docRefDepto = FirebaseFirestore.getInstance().document("Departamentos/${nuevoDepto.id}")
                    campos["Departamento"] = docRefDepto
                }
            }
            if (campos.isEmpty()) {
                showToast("No hay cambios para guardar")
                return@setOnClickListener
            }
            // Mostrar confirmación antes de guardar
            mostrarDialogoConfirmacionEdicion(empleado, campos, listaDeptos) {
                empleadosViewModel.actualizarEmpleado(empleado, campos) { exito ->
                    if (exito) showToast("Empleado actualizado correctamente")
                    else showToast("Error al actualizar")
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun mostrarDialogoConfirmacionEdicion(
        empleado: Empleado,
        campos: Map<String, Any>,
        listaDeptos: List<Departamento>,
        onConfirm: () -> Unit
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm, null)
        val tvMensaje = dialogView.findViewById<TextView>(R.id.tvConfirmMessage)
        val etRegistro = dialogView.findViewById<TextView>(R.id.etRegistro)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

        // Armamos el resumen solo de los campos que cambian
        val resumen = StringBuilder()
        campos.forEach { (key, value) ->
            when (key) {
                "Email" -> resumen.append("Email: ${empleado.email} → $value\n")
                "Departamento" -> {
                    val nuevoId = (value as? com.google.firebase.firestore.DocumentReference)?.id ?: value.toString()
                    val nombreAntiguo = listaDeptos.firstOrNull { it.id == empleado.departamento }?.nombre ?: empleado.departamento
                    val nombreNuevo = listaDeptos.firstOrNull { it.id == nuevoId }?.nombre ?: nuevoId
                    resumen.append("Departamento: $nombreAntiguo → $nombreNuevo\n")
                }
                // Si en el futuro añades más campos editables, los añades aquí.
            }
        }

        tvMensaje.text = "¿Deseas guardar los siguientes cambios?"
        etRegistro.text = resumen.toString().trim()
        etRegistro.isEnabled = false // Solo lectura

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }
        dialog.show()
    }

    private fun mostrarDetalleEmpleado(empleado: Empleado, contenedor: FrameLayout) {
        val detalleView = layoutInflater.inflate(R.layout.detalle_empleado, null)
        detalleView.findViewById<TextView>(R.id.tvDetalleNombre).text = empleado.nombre
        detalleView.findViewById<TextView>(R.id.tvDetalleEmail).text = empleado.email
        detalleView.findViewById<TextView>(R.id.tvDetalleDocumento).text =
            "${empleado.tipoDocumento}: ${empleado.numDocumento}"

        // Listener de EDITAR (rol desde SessionManager)
        detalleView.findViewById<ImageView>(R.id.btnEditarEmpleado).setOnClickListener {
            // Si ya están cargados, mostrar el diálogo
            val listaDeptos = departamentosViewModel.departamentos.value
            if (!listaDeptos.isNullOrEmpty()) {
                val rolUsuario = SessionManager.currentEmpleado?.departamento?.lowercase() ?: ""
                mostrarDialogoEditarEmpleado(empleado, listaDeptos, rolUsuario)
            } else {
                // Lanzamos carga y escuchamos
                Log.d("DEBUG", "Departamentos para editar: ${listaDeptos?.map { it.nombre }}")
                departamentosViewModel.cargarDepartamentos()
                departamentosViewModel.departamentos.observe(viewLifecycleOwner) { nuevosDeptos ->
                    if (!nuevosDeptos.isNullOrEmpty()) {
                        val rolUsuario = SessionManager.currentEmpleado?.departamento?.lowercase() ?: ""
                        mostrarDialogoEditarEmpleado(empleado, nuevosDeptos, rolUsuario)
                    }
                }
                showToast("Cargando departamentos, intenta de nuevo en un segundo")
            }
        }

        detalleView.findViewById<ImageView>(R.id.btnDesactivarEmpleado).setOnClickListener {
            mostrarDialogoDesactivarEmpleado(empleado)
        }

        // Cargar departamento y oficina
        lifecycleScope.launch {
            val nombreDepto = departamentosViewModel.departamentos.value?.firstOrNull { it.id == empleado.departamento }?.nombre
            val datosOfi = oficinasViewModel.oficinasFiltradas.value?.firstOrNull { it.id == empleado.oficina }
            detalleView.findViewById<TextView>(R.id.tvDetalleDepartamento).text =
                "Departamento: ${nombreDepto ?: "Desconocido"}"
            detalleView.findViewById<TextView>(R.id.tvDetalleOficina).text =
                "Oficina: ${datosOfi?.direccion ?: "Desconocida"}, ${datosOfi?.ciudad ?: ""}"
        }

        contenedor.removeAllViews()
        contenedor.addView(detalleView)
    }

    private fun mostrarDialogoCrearEmpleado(
        listaDepartamentos: List<Departamento>,
        listaOficinas: List<Oficina>
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nuevo_empleado, null)

        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreEmpleado)
        val spDepartamento = dialogView.findViewById<Spinner>(R.id.spDepartamento)
        val spTipoDocumento = dialogView.findViewById<Spinner>(R.id.spTipoDocumento)
        val etNumeroDocumento = dialogView.findViewById<EditText>(R.id.etNumeroDocumento)
        val spOficina = dialogView.findViewById<Spinner>(R.id.spOficina)

        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarNuevo)
        val btnCrear = dialogView.findViewById<Button>(R.id.btnCrearNuevo)

        // Tipo de documento
        val tiposDoc = listOf("NIF", "NIE")
        spTipoDocumento.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tiposDoc)

        // Adaptador personalizado para Departamento (mostrar nombre)
        val adapterDepto = object : ArrayAdapter<Departamento>(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, listaDepartamentos
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).text = getItem(position)?.nombre ?: ""
                return view
            }
            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).text = getItem(position)?.nombre ?: ""
                return view
            }
        }
        spDepartamento.adapter = adapterDepto

        // Adaptador personalizado para Oficina (mostrar ciudad + dirección)
        val adapterOfi = object : ArrayAdapter<Oficina>(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, listaOficinas
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).text = "${getItem(position)?.ciudad} - ${getItem(position)?.direccion}"
                return view
            }
            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).text = "${getItem(position)?.ciudad} - ${getItem(position)?.direccion}"
                return view
            }
        }
        spOficina.adapter = adapterOfi

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnCrear.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val tipoDoc = spTipoDocumento.selectedItem.toString()
            val docNum = etNumeroDocumento.text.toString().trim()

            if (nombre.isEmpty() || docNum.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val departamento = spDepartamento.selectedItem as Departamento
            val oficina = spOficina.selectedItem as Oficina

            val nuevoEmpleado = Empleado(
                id = "",
                nombre = nombre,
                codigo = "",
                tipoDocumento = tipoDoc,
                numDocumento = docNum,
                email = "",
                departamento = departamento.id,
                oficina = oficina.id,
                activo = true,
                motivoBaja = ""
            )

            dialog.dismiss()
            mostrarDialogoConfirmacionCreacion(nuevoEmpleado)
        }

        dialog.show()
    }

    private fun mostrarDialogoConfirmacionCreacion(empleado: Empleado) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm, null)
        val tvMensaje = dialogView.findViewById<TextView>(R.id.tvConfirmMessage)
        val etResumen = dialogView.findViewById<TextView>(R.id.etRegistro)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

        val resumen = """
        Nombre: ${empleado.nombre}
        Tipo Doc: ${empleado.tipoDocumento}
        Nº Documento: ${empleado.numDocumento}
        Departamento: ${empleado.departamento}
        Oficina: ${empleado.oficina}
    """.trimIndent()

        tvMensaje.text = "¿Deseas crear este nuevo empleado?"
        etResumen.text = resumen
        etResumen.isEnabled = false // No editable

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnConfirmar.setOnClickListener {
            dialog.dismiss()
            empleadosViewModel.crearEmpleado(empleado) { exito ->
                if (exito) {
                    Toast.makeText(requireContext(), "Empleado creado correctamente", Toast.LENGTH_SHORT).show()
                    empleadosViewModel.cargarEmpleados()
                } else {
                    Toast.makeText(requireContext(), "Error al crear el empleado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }


    private fun exportarEmpleadosCSV() {
        val builder = StringBuilder()
        val fileName: String
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
        val data = empleadosViewModel.empleadosFiltrados.value

        if (data.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No hay empleados para exportar.", Toast.LENGTH_SHORT).show()
            return
        }
        builder.append("ID,Codigo,Nombre,Email,Departamento,Oficina\n")
        data.forEach {
            builder.append("${it.id},${it.codigo},${it.nombre},${it.email},${it.departamento},${it.oficina}\n")
        }

        fileName = "Empleados_Export_$timestamp.csv"
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            file.writeText(builder.toString())
            Toast.makeText(requireContext(), "CSV exportado en: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al exportar CSV", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * Diálogo de desactivación (puedes mover esto a un Helper si quieres reusar)
     */
    private fun mostrarDialogoDesactivarEmpleado(empleado: Empleado) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_desactivar, null)
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTituloDesactivar)
        val etMotivo = dialogView.findViewById<EditText>(R.id.etMotivoBaja)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarDesactivar)
        val btnDesactivar = dialogView.findViewById<Button>(R.id.btnConfirmarDesactivar)

        tvTitulo.text = "${empleado.nombre} (Desactivar)"

        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
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
                desactivarEmpleado(empleado, motivo)
            }
        }

        alertDialog.show()
    }

    private fun desactivarEmpleado(empleado: Empleado, motivo: String) {
        lifecycleScope.launch {
            val exito = empleadosViewModel.desactivarEmpleado(empleado.id, motivo)
            if (exito) {
                showToast("Empleado desactivado")
                empleadosViewModel.cargarEmpleados()
            } else {
                showToast("Error al desactivar")
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}
