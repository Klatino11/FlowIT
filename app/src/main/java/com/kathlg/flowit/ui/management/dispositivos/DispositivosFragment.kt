package com.kathlg.flowit.ui.management.dispositivos

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.kathlg.flowit.R
import com.kathlg.flowit.data.model.Dispositivo
import com.kathlg.flowit.data.model.Empleado
import com.kathlg.flowit.data.model.TipoDispositivo
import com.kathlg.flowit.data.repository.DispositivosRepository
import com.kathlg.flowit.data.repository.EmpleadosRepository
import com.kathlg.flowit.data.repository.OficinasRepository
import com.kathlg.flowit.data.repository.TiposDispositivosRepository
import com.kathlg.flowit.ui.management.empleados.EmpleadoViewModelFactory
import com.kathlg.flowit.ui.management.empleados.EmpleadosViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DispositivosFragment : Fragment() {

    private val dispositivosViewModel: DispositivosViewModel by activityViewModels {
        DispositivosViewModelFactory(DispositivosRepository())
    }
    private val empleadosViewModel: EmpleadosViewModel by activityViewModels {
        EmpleadoViewModelFactory(EmpleadosRepository())
    }
    private var tiposList: List<TipoDispositivo> = emptyList()
    private var mapEmpleados: Map<String, String> = emptyMap()
    private lateinit var dispAdapter: DispositivoAdapter
    private var dispositivoSeleccionado: Dispositivo? = null
    private var mapTipos: Map<String, String> = emptyMap() // <ID, Nombre>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dispositivos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rvListado = view.findViewById<RecyclerView>(R.id.rvListado)
        val etBuscar = view.findViewById<EditText>(R.id.etBuscar)
        val contenedor = view.findViewById<FrameLayout>(R.id.flDetalles)
        val fabCrear = view.findViewById<FloatingActionButton>(R.id.fabCrearRegistro)
        val btnExportar = view.findViewById<FloatingActionButton>(R.id.fabExportarCSV)
        btnExportar.setOnClickListener {
            exportarDispositivosCSV()
        }
        val btnFiltro = view.findViewById<ImageView>(R.id.ivFiltrar)
        btnFiltro.setOnClickListener {
            mostrarDialogoFiltroDispositivo()
        }

        fabCrear.isEnabled = false // ¡Importante!

        // Inicializa el adapter vacío
        dispAdapter = DispositivoAdapter(emptyList(), mapEmpleados) { dispositivo ->
            dispositivoSeleccionado = dispositivo
            mostrarDetalleDispositivo(dispositivo, contenedor)
        }
        rvListado.layoutManager = LinearLayoutManager(requireContext())
        rvListado.adapter = dispAdapter

        empleadosViewModel.cargarEmpleados()
        // Observa empleados y dispositivos, y actualiza el adapter en cada cambio
        empleadosViewModel.empleadosFiltrados.observe(viewLifecycleOwner) { listaEmpleados ->
            mapEmpleados = listaEmpleados.associate { it.codigo to it.nombre }
            Log.d("EmpleadosMapDebug", "Map keys: ${mapEmpleados.keys}")
            val dispositivos = dispositivosViewModel.devices.value ?: emptyList()
            dispAdapter.updateData(dispositivos, mapEmpleados)
            // Refresca el detalle si hay uno seleccionado
            dispositivoSeleccionado?.let {
                mostrarDetalleDispositivo(it, contenedor)
            }
        }

        dispositivosViewModel.devices.observe(viewLifecycleOwner) { dispositivos ->// Solo muestra los activos al iniciar
            val activos = dispositivos.filter { it.activo }
            dispAdapter.updateData(activos, mapEmpleados)
        }

        // Carga los tipos UNA VEZ y habilita el botón solo cuando termines
        lifecycleScope.launch {
            val tiposRepo = TiposDispositivosRepository()
            tiposList = tiposRepo.obtenerTiposDispositivos()
            Log.d("TiposDebug", "tiposList cargados: ${tiposList.size}")
            mapTipos = tiposList.associate { it.id to it.nombre }
            fabCrear.isEnabled = true
        }


        fabCrear.setOnClickListener {
            if (tiposList.isEmpty()) {
                showToast("Cargando tipos de dispositivo, espera un momento...")
            } else {
                mostrarDialogoNuevoDispositivo()
            }
        }

        dispositivosViewModel.loadDevices()

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val texto = s?.toString()?.trim()?.lowercase() ?: ""
                val dispositivosOriginales = dispositivosViewModel.devices.value ?: emptyList()
                val filtrados = if (texto.isBlank()) {
                    dispositivosOriginales
                } else {
                    dispositivosOriginales.filter {
                        it.nombre.lowercase().contains(texto)
                    }
                }
                dispAdapter.updateData(filtrados, mapEmpleados)
            }
        })
    }

    private fun mostrarDialogoFiltroDispositivo() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filtro_dispositivo, null)
        val spTipo = dialogView.findViewById<Spinner>(R.id.spFiltroTipo)
        val etRam = dialogView.findViewById<EditText>(R.id.etFiltroRAM)
        val etModelo = dialogView.findViewById<EditText>(R.id.etFiltroModelo)
        val etSO = dialogView.findViewById<EditText>(R.id.etFiltroSO)
        val spActivo = dialogView.findViewById<Spinner>(R.id.spFiltroActivo)

        // Rellenar spinners
        val adapterTipo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("Cualquiera") + tiposList.map { it.nombre })
        spTipo.adapter = adapterTipo

        val adapterActivo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("Cualquiera", "Sí", "No"))
        spActivo.adapter = adapterActivo

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancelarFiltro).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnAplicarFiltro).setOnClickListener {
            // Recoger valores
            val tipoSeleccionado = spTipo.selectedItemPosition
            val tipoFiltro = if (tipoSeleccionado > 0) tiposList[tipoSeleccionado - 1].id else null
            val ramFiltro = etRam.text.toString().toIntOrNull()
            val modeloFiltro = etModelo.text.toString().trim().lowercase()
            val soFiltro = etSO.text.toString().trim().lowercase()
            val activoFiltro = when (spActivo.selectedItemPosition) {
                1 -> true
                2 -> false
                else -> null
            }
            aplicarFiltroDispositivos(tipoFiltro, ramFiltro, modeloFiltro, soFiltro, activoFiltro)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun aplicarFiltroDispositivos(
        tipoId: String?,
        ram: Int?,
        modelo: String?,
        so: String?,
        activo: Boolean?
    ) {
        val originales = dispositivosViewModel.devices.value ?: emptyList()
        val filtrados = originales.filter { d ->
            (tipoId == null || d.tipo == tipoId) &&
                    (ram == null || d.ramGb == ram) &&
                    (modelo.isNullOrBlank() || d.modelo.lowercase().contains(modelo)) &&
                    (so.isNullOrBlank() || (d.sistemaOperativo ?: "").lowercase().contains(so)) &&
                    (activo == null || d.activo == activo)
        }
        dispAdapter.updateData(filtrados, mapEmpleados)
    }



    private fun mostrarDialogoNuevoDispositivo() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nuevo_dispositivo, null)

        // Referencias a vistas
        val spTipoDispositivo = dialogView.findViewById<Spinner>(R.id.spTipoDispositivo)
        val etRamDispositivo = dialogView.findViewById<EditText>(R.id.etRamDispositivo)
        val etModeloDispositivo = dialogView.findViewById<EditText>(R.id.etModeloDispositivo)
        val etMarcaDispositivo = dialogView.findViewById<EditText>(R.id.etMarcaDispositivo)
        val etNumSerie = dialogView.findViewById<EditText>(R.id.etNumSerie)
        val etPrecioDispositivo = dialogView.findViewById<EditText>(R.id.etPrecioDispositivo)
        val layoutTeamViewer = dialogView.findViewById<LinearLayout>(R.id.layoutTeamViewer)
        val swTeamViewer = dialogView.findViewById<Switch>(R.id.swTeamViewer)
        val layoutSO = dialogView.findViewById<LinearLayout>(R.id.layoutSO)
        val etSO = dialogView.findViewById<EditText>(R.id.etSO)
        val layoutNumTelefono = dialogView.findViewById<LinearLayout>(R.id.layoutNumTelefono)
        val etNumTelefono = dialogView.findViewById<EditText>(R.id.etNumTelefono)
        val spAsignacion = dialogView.findViewById<Spinner>(R.id.spAsignacion)
        val layoutDeepFreeze = dialogView.findViewById<LinearLayout>(R.id.layoutDeepFreeze)
        val swDeepFreeze = dialogView.findViewById<Switch>(R.id.swDeepFreeze)

        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarNuevo)
        val btnCrear = dialogView.findViewById<Button>(R.id.btnCrearNuevo)

        // 1. Cargar tipos de dispositivo
        val tiposRepo = TiposDispositivosRepository()
        val empleadosRepo = EmpleadosRepository()
        val dispositivosRepo = DispositivosRepository()

        lifecycleScope.launch {
            val empleadosList = empleadosRepo.obtenerEmpleados().filter { it.activo }
            val dispositivosList = dispositivosRepo.obtenerDispositivos() // Para calcular el siguiente número

            // Adaptador para tipos de dispositivo (SOLO USA LA LISTA YA CARGADA)
            val adapterTipo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tiposList.map { it.nombre })
            spTipoDispositivo.adapter = adapterTipo

            // Adaptador para empleados...
            val empleadosConVacio = listOf("Sin asignar") + empleadosList.map { "${it.codigo} - ${it.nombre}" }
            spAsignacion.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, empleadosConVacio)

            // Al cambiar tipo...
            spTipoDispositivo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val tipoSeleccionado = tiposList[position].nombre.lowercase()
                    if (tipoSeleccionado.contains("móvil") || tipoSeleccionado.contains("movil")) {
                        layoutTeamViewer.visibility = View.VISIBLE
                        layoutNumTelefono.visibility = View.VISIBLE
                        layoutSO.visibility = View.GONE
                        layoutDeepFreeze.visibility = View.GONE
                    } else if (tipoSeleccionado.contains("portátil") || tipoSeleccionado.contains("portatil") || tipoSeleccionado.contains("sobremesa")) {
                        layoutTeamViewer.visibility = View.GONE
                        layoutNumTelefono.visibility = View.GONE
                        layoutSO.visibility = View.VISIBLE
                        layoutDeepFreeze.visibility = View.VISIBLE
                    } else {
                        layoutTeamViewer.visibility = View.GONE
                        layoutNumTelefono.visibility = View.GONE
                        layoutSO.visibility = View.GONE
                        layoutDeepFreeze.visibility = View.GONE
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create()

            btnCancelar.setOnClickListener { dialog.dismiss() }
            btnCrear.setOnClickListener {
                // Validación básica
                val tipoIdx = spTipoDispositivo.selectedItemPosition
                val tipo = tiposList.getOrNull(tipoIdx)
                val ram = etRamDispositivo.text.toString().toIntOrNull()
                val modelo = etModeloDispositivo.text.toString().trim()
                val marca = etMarcaDispositivo.text.toString().trim()
                val numSerie = etNumSerie.text.toString().trim()
                val precio = etPrecioDispositivo.text.toString().toDoubleOrNull()
                val so = etSO.text.toString().trim()
                    .takeIf { layoutSO.visibility == View.VISIBLE && it.isNotBlank() }
                val teamViewer = swTeamViewer.isChecked
                val numTelefono = etNumTelefono.text.toString().trim()
                    .takeIf { layoutNumTelefono.visibility == View.VISIBLE && it.isNotBlank() }

                if (tipo == null || ram == null || modelo.isEmpty() || marca.isEmpty() || numSerie.isEmpty() || precio == null) {
                    Toast.makeText(
                        requireContext(),
                        "Completa todos los campos obligatorios",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // Obtener el prefijo
                val prefijo = tipo.prefijo
                // Buscar el siguiente número para este prefijo
                val existentes = dispositivosList.filter { it.nombre.startsWith(prefijo) }
                val numeros =
                    existentes.mapNotNull { it.nombre.removePrefix(prefijo).toIntOrNull() }
                val siguienteNumero = (numeros.maxOrNull() ?: 0) + 1
                val nombreDispositivo = prefijo + siguienteNumero.toString().padStart(4, '0')

                // Asignación: obtener referencia si no es vacío
                val asignacionIdx = spAsignacion.selectedItemPosition
                val empleadoAsignado: String? =
                    if (asignacionIdx > 0) empleadosList[asignacionIdx - 1].id else null

                // Crear el mapa para Firestore
                val firestore = FirebaseFirestore.getInstance()
                val data = mutableMapOf<String, Any>(
                    "TipoDispositivo" to firestore.collection("TiposDispositivos")
                        .document(tipo.id),
                    "RAM" to ram,
                    "Modelo" to modelo,
                    "Marca" to marca,
                    "NumSerie" to numSerie,
                    "Precio" to precio
                )
                if (so != null) data["SO"] = so
                if (layoutTeamViewer.visibility == View.VISIBLE) data["TeamViewer"] = teamViewer
                val deepFreeze = swDeepFreeze.isChecked
                if (layoutDeepFreeze.visibility == View.VISIBLE) data["Deep Freeze"] = deepFreeze
                if (numTelefono != null) data["NumTelefono"] = numTelefono
                if (empleadoAsignado != null) data["Asignacion"] =
                    firestore.collection("Empleados").document(empleadoAsignado)
                data["Activo"] = true

                // Guardar en Firestore
                lifecycleScope.launch {
                    try {
                        firestore.collection("Dispositivos")
                            .document(nombreDispositivo)
                            .set(data)
                            .await()
                        Toast.makeText(requireContext(), "Dispositivo creado", Toast.LENGTH_SHORT).show()
                        dispositivosViewModel.loadDevices()
                        empleadosViewModel.cargarEmpleados()
                        delay(300) // <- da un pequeño margen antes de cerrar el diálogo (opcional)
                        dialog.dismiss()
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Error al crear: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        dialog.show()
    }
}


    private fun exportarDispositivosCSV() {
        val builder = StringBuilder()
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
        val fileName = "Dispositivos_Export_$timestamp.csv"
        val data = dispositivosViewModel.devices.value

        if (data.isNullOrEmpty()) {
            showToast("No hay dispositivos para exportar.")
            return
        }

        // Cabecera CSV
        builder.append("Nombre,Tipo,RAM,Modelo,Marca,Nº Serie,Precio,Empleado,SO,DeepFreeze,TeamViewer,Núm. Teléfono\n")

        // Filas
        data.forEach { d ->
            val tipoNombre = mapTipos[d.tipo] ?: d.tipo
            val nombreEmpleado = mapEmpleados[d.codigoEmpleado] ?: "Sin asignar"
            val deepFreeze = if (d.deepFreezeInstalado) "Sí" else "No"
            val teamViewer = if (d.teamviewerInstalado) "Sí" else "No"
            builder.appendLine(
                "${d.nombre},$tipoNombre,${d.ramGb},${d.modelo},${d.marca},${d.numeroSerie},${d.precio}," +
                        "$nombreEmpleado,${d.sistemaOperativo ?: ""},$deepFreeze,$teamViewer,${d.numeroTelefono ?: ""}"
            )
        }

        try {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            file.writeText(builder.toString())
            showToast("CSV exportado en: ${file.absolutePath}")
        } catch (e: Exception) {
            showToast("Error al exportar CSV: ${e.message}")
            e.printStackTrace()
        }
    }



    private fun mostrarDetalleDispositivo(dispositivo: Dispositivo, contenedor: FrameLayout) {
        val detalleView = layoutInflater.inflate(R.layout.detalle_dispositivo, null)
        Log.d("EmpleadoDebug", "codigo=${dispositivo.codigoEmpleado}, nombre=${mapEmpleados[dispositivo.codigoEmpleado]}")
        // Setea los campos básicos
        detalleView.findViewById<TextView>(R.id.tvDetalleNombre).text = dispositivo.nombre
        Log.d("Dispositivo", "tipo=${dispositivo.tipo}, nombreTipo=${mapTipos[dispositivo.tipo]}")
        val nombreTipo = mapTipos[dispositivo.tipo] ?: dispositivo.tipo
        detalleView.findViewById<TextView>(R.id.tvDetalleTipo).text = "Tipo: $nombreTipo"
        detalleView.findViewById<TextView>(R.id.tvDetalleRam).text = "RAM: ${dispositivo.ramGb} GB"
        detalleView.findViewById<TextView>(R.id.tvDetalleModelo).text = "Modelo: ${dispositivo.modelo}"
        detalleView.findViewById<TextView>(R.id.tvDetalleMarca).text = "Marca: ${dispositivo.marca}"
        detalleView.findViewById<TextView>(R.id.tvDetalleNumSerie).text = "Nº Serie: ${dispositivo.numeroSerie}"
        detalleView.findViewById<TextView>(R.id.tvDetallePrecio).text = "Precio: %.2f €".format(dispositivo.precio)

        // Empleado asignado
        val nombreEmpleado = mapEmpleados[dispositivo.codigoEmpleado] ?: dispositivo.codigoEmpleado

        detalleView.findViewById<TextView>(R.id.tvDetalleEmpleado).text = "Empleado: $nombreEmpleado"
        Log.d("EmpleadoDebug", "codigo=${dispositivo.codigoEmpleado}, nombre=${mapEmpleados[dispositivo.codigoEmpleado]}")

        // ---- Mostrar/ocultar campos según tipo ----
        val tvSO = detalleView.findViewById<TextView>(R.id.tvDetalleSO)
        val tvDeepFreeze = detalleView.findViewById<TextView>(R.id.tvDetalleDeepFreeze)
        val tvTeamViewer = detalleView.findViewById<TextView>(R.id.tvDetalleTeamViewer)
        val tvNumTelefono = detalleView.findViewById<TextView>(R.id.tvDetalleNumTelefono)

        when (dispositivo.tipo.lowercase()) {
            "móvil", "movil" -> {
                // Oculta campos de SO y DeepFreeze
                tvSO.visibility = View.GONE
                tvDeepFreeze.visibility = View.GONE

                // Muestra y rellena TeamViewer y número de teléfono
                tvTeamViewer.visibility = View.VISIBLE
                tvNumTelefono.visibility = View.VISIBLE
                tvTeamViewer.text = "TeamViewer: " + if (dispositivo.teamviewerInstalado) "Sí" else "No"
                tvNumTelefono.text = "Teléfono: ${dispositivo.numeroTelefono ?: "-"}"
            }
            "portátil", "portatil", "sobremesa" -> {
                // Muestra SO y DeepFreeze
                tvSO.visibility = View.VISIBLE
                tvDeepFreeze.visibility = View.VISIBLE
                tvSO.text = "SO: ${dispositivo.sistemaOperativo ?: "-"}"
                tvDeepFreeze.text = "Deep Freeze: " + if (dispositivo.deepFreezeInstalado) "Sí" else "No"

                // Oculta campos de móvil
                tvTeamViewer.visibility = View.GONE
                tvNumTelefono.visibility = View.GONE
            }
            else -> {
                // Oculta todos los campos opcionales por defecto
                tvSO.visibility = View.GONE
                tvDeepFreeze.visibility = View.GONE
                tvTeamViewer.visibility = View.GONE
                tvNumTelefono.visibility = View.GONE
            }
        }

        detalleView.findViewById<ImageView>(R.id.btnDesactivarDispositivo).setOnClickListener {
            mostrarDialogoDesactivarDispositivo(dispositivo)
        }

        detalleView.findViewById<ImageView>(R.id.btnEditarDispositivo).setOnClickListener {
            mostrarDialogoEditarDispositivo(dispositivo)
        }

        contenedor.removeAllViews()
        contenedor.addView(detalleView)
    }

    private fun mostrarDialogoEditarDispositivo(dispositivo: Dispositivo) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_dispositivo, null)
        val etRAM = dialogView.findViewById<EditText>(R.id.etEditarRAM)
        val spAsignacion = dialogView.findViewById<Spinner>(R.id.spEditarAsignacion)
        val layoutTeamViewer = dialogView.findViewById<LinearLayout>(R.id.layoutEditarTeamViewer)
        val swTeamViewer = dialogView.findViewById<Switch>(R.id.swEditarTeamViewer)
        val layoutSO = dialogView.findViewById<LinearLayout>(R.id.layoutEditarSO)
        val etSO = dialogView.findViewById<EditText>(R.id.etEditarSO)
        val layoutDeepFreeze = dialogView.findViewById<LinearLayout>(R.id.layoutEditarDeepFreeze)
        val swDeepFreeze = dialogView.findViewById<Switch>(R.id.swEditarDeepFreeze)
        val layoutNumTelefono = dialogView.findViewById<LinearLayout>(R.id.layoutEditarNumTelefono)
        val etNumTelefono = dialogView.findViewById<EditText>(R.id.etEditarNumTelefono)

        // Setear valores actuales
        etRAM.setText(dispositivo.ramGb.toString())
        swTeamViewer.isChecked = dispositivo.teamviewerInstalado
        etSO.setText(dispositivo.sistemaOperativo ?: "")
        swDeepFreeze.isChecked = dispositivo.deepFreezeInstalado
        etNumTelefono.setText(dispositivo.numeroTelefono ?: "")

        val tipoNombre = mapTipos[dispositivo.tipo]?.lowercase() ?: ""
        layoutTeamViewer.visibility = if (tipoNombre.contains("móvil") || tipoNombre.contains("movil")) View.VISIBLE else View.GONE
        layoutNumTelefono.visibility = if (tipoNombre.contains("móvil") || tipoNombre.contains("movil")) View.VISIBLE else View.GONE
        layoutSO.visibility = if (tipoNombre.contains("portátil") || tipoNombre.contains("portatil") || tipoNombre.contains("sobremesa")) View.VISIBLE else View.GONE
        layoutDeepFreeze.visibility = if (tipoNombre.contains("portátil") || tipoNombre.contains("portatil") || tipoNombre.contains("sobremesa")) View.VISIBLE else View.GONE

        var empleadosList: List<Empleado> = emptyList() // <-- Declara fuera

        lifecycleScope.launch {
            val empleadosRepo = EmpleadosRepository()
            empleadosList = empleadosRepo.obtenerEmpleados().filter { it.activo }

            val empleadosConVacio = listOf("Sin asignar") + empleadosList.map { "${it.codigo} - ${it.nombre}" }
            spAsignacion.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, empleadosConVacio)
            val idxAsignacion = empleadosList.indexOfFirst { it.codigo == dispositivo.codigoEmpleado }
            spAsignacion.setSelection(if (idxAsignacion != -1) idxAsignacion + 1 else 0)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancelarEdicionDispositivo).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnGuardarEdicionDispositivo).setOnClickListener {
            val nuevaRam = etRAM.text.toString().toIntOrNull()
            val nuevaAsignacionIdx = spAsignacion.selectedItemPosition
            val nuevoTeamViewer = swTeamViewer.isChecked
            val nuevoSO = etSO.text.toString().trim()
            val nuevoDeepFreeze = swDeepFreeze.isChecked
            val nuevoNumTelefono = etNumTelefono.text.toString().trim()

            val hayCambios =
                nuevaRam != dispositivo.ramGb ||
                        (layoutTeamViewer.visibility == View.VISIBLE && nuevoTeamViewer != dispositivo.teamviewerInstalado) ||
                        (layoutSO.visibility == View.VISIBLE && nuevoSO != (dispositivo.sistemaOperativo ?: "")) ||
                        (layoutDeepFreeze.visibility == View.VISIBLE && nuevoDeepFreeze != dispositivo.deepFreezeInstalado) ||
                        (layoutNumTelefono.visibility == View.VISIBLE && nuevoNumTelefono != (dispositivo.numeroTelefono ?: "")) ||
                        (nuevaAsignacionIdx != (empleadosList.indexOfFirst { it.codigo == dispositivo.codigoEmpleado } + 1)) // Detectar cambio en asignación

            if (!hayCambios) {
                showToast("No hay cambios para guardar.")
                return@setOnClickListener
            }

            dialog.dismiss()
            mostrarDialogoConfirmarEdicion(
                dispositivo,
                nuevaRam,
                nuevaAsignacionIdx,
                empleadosList,
                nuevoTeamViewer,
                nuevoSO,
                nuevoDeepFreeze,
                nuevoNumTelefono
            )
        }
        dialog.show()
    }


    private fun mostrarDialogoConfirmarEdicion(
        dispositivo: Dispositivo,
        nuevaRam: Int?,
        nuevaAsignacionIdx: Int,
        empleadosList: List<Empleado>,
        nuevoTeamViewer: Boolean,
        nuevoSO: String,
        nuevoDeepFreeze: Boolean,
        nuevoNumTelefono: String
    )
    {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm, null)
        val tvMensaje = dialogView.findViewById<TextView>(R.id.tvConfirmMessage)
        val etRegistro = dialogView.findViewById<TextView>(R.id.etRegistro)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

        // Muestra un resumen de cambios (adáptalo a lo que muestres)
        etRegistro.text = """
        Código: ${dispositivo.nombre}
        RAM: ${nuevaRam ?: dispositivo.ramGb} GB
        Asignación: ...
        Oficina: ...
        TeamViewer: $nuevoTeamViewer
        SO: $nuevoSO
        Deep Freeze: $nuevoDeepFreeze
        Número Teléfono: $nuevoNumTelefono
    """.trimIndent()

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            dialog.dismiss()
            // Guardar cambios
            guardarCambiosDispositivo(
                dispositivo,
                nuevaRam,
                nuevaAsignacionIdx,
                empleadosList, // pásala aquí también
                nuevoTeamViewer,
                nuevoSO,
                nuevoDeepFreeze,
                nuevoNumTelefono
            )

        }
        dialog.show()
    }

    private fun guardarCambiosDispositivo(
        dispositivo: Dispositivo,
        nuevaRam: Int?,
        nuevaAsignacionIdx: Int,
        empleadosList: List<Empleado>,
        nuevoTeamViewer: Boolean,
        nuevoSO: String,
        nuevoDeepFreeze: Boolean,
        nuevoNumTelefono: String
    ) {
        val cambios = mutableMapOf<String, Any>()
        nuevaRam?.let { cambios["RAM"] = it }

        // Asignación (referencia a empleado)
        if (nuevaAsignacionIdx > 0) {
            val empleadoAsignado = empleadosList[nuevaAsignacionIdx - 1]
            cambios["Asignacion"] = FirebaseFirestore.getInstance()
                .collection("Empleados")
                .document(empleadoAsignado.id)
        } else {
            cambios["Asignacion"] = FieldValue.delete() // Si quieres quitar la asignación
        }

        if (nuevoSO.isNotBlank()) cambios["SO"] = nuevoSO
        cambios["TeamViewer"] = nuevoTeamViewer
        cambios["Deep Freeze"] = nuevoDeepFreeze
        cambios["NumTelefono"] = nuevoNumTelefono

        lifecycleScope.launch {
            try {
                FirebaseFirestore.getInstance().collection("Dispositivos")
                    .document(dispositivo.nombre)
                    .update(cambios)
                    .await()
                showToast("Dispositivo actualizado")
                dispositivosViewModel.loadDevices()
            } catch (e: Exception) {
                showToast("Error al guardar: ${e.message}")
            }
        }
    }




    private fun mostrarDialogoDesactivarDispositivo(dispositivo: Dispositivo) {
        // PRIMER DIALOG: Motivo de baja
        val dialogView = layoutInflater.inflate(R.layout.dialog_desactivar, null)
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTituloDesactivar)
        val etMotivo = dialogView.findViewById<EditText>(R.id.etMotivoBaja)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarDesactivar)
        val btnDesactivar = dialogView.findViewById<Button>(R.id.btnConfirmarDesactivar)

        tvTitulo.text = "${dispositivo.nombre} (Desactivar)"

        val primerDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancelar.setOnClickListener { primerDialog.dismiss() }

        btnDesactivar.setOnClickListener {
            val motivo = etMotivo.text.toString().trim()
            if (motivo.isEmpty()) {
                showToast("Por favor, introduce un motivo")
            } else {
                primerDialog.dismiss()
                // SEGUNDO DIALOG: Confirmación
                mostrarDialogoConfirmacionDesactivacion(dispositivo, motivo)
            }
        }

        primerDialog.show()
    }

    private fun mostrarDialogoConfirmacionDesactivacion(dispositivo: Dispositivo, motivo: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm, null)
        val tvMensaje = dialogView.findViewById<TextView>(R.id.tvConfirmMessage)
        val etRegistro = dialogView.findViewById<TextView>(R.id.etRegistro)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

        // Mensaje principal
        tvMensaje.text = "¿Seguro que deseas desactivar a este empleado?"

        // Resumen de la acción (puedes personalizar los datos que muestras aquí)
        etRegistro.text = """
            Código: ${dispositivo.nombre}
            Tipo: ${mapTipos[dispositivo.tipo] ?: dispositivo.tipo}
            RAM: ${dispositivo.ramGb} GB
            Modelo: ${dispositivo.modelo}
            Marca: ${dispositivo.marca}
            Nº Serie: ${dispositivo.numeroSerie}
            Empleado asignado: ${mapEmpleados[dispositivo.codigoEmpleado] ?: "Sin asignar"}
            Estado: ${if (dispositivo.activo) "Activo" else "No activo"}
            Motivo: $motivo
        """.trimIndent()

        etRegistro.isEnabled = false // Solo lectura

        val segundoDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancelar.setOnClickListener { segundoDialog.dismiss() }
        btnConfirmar.setOnClickListener {
            segundoDialog.dismiss()
            // Ya con el motivo, desactiva realmente
            desactivarDispositivo(dispositivo, motivo)
        }

        segundoDialog.show()
    }
    private fun desactivarDispositivo(dispositivo: Dispositivo, motivo: String) {
        lifecycleScope.launch {
            val exito = dispositivosViewModel.desactivarDispositivo(dispositivo.nombre, motivo)
            if (exito) {
                showToast("Dispositivo desactivado")
                empleadosViewModel.cargarEmpleados()
            } else {
                showToast("Error al desactivar")
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
