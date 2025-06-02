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
import com.google.firebase.firestore.FirebaseFirestore
import com.kathlg.flowit.R
import com.kathlg.flowit.data.model.Dispositivo
import com.kathlg.flowit.data.model.TipoDispositivo
import com.kathlg.flowit.data.repository.DispositivosRepository
import com.kathlg.flowit.data.repository.EmpleadosRepository
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

        dispositivosViewModel.devices.observe(viewLifecycleOwner) { dispositivos ->
            dispAdapter.updateData(dispositivos, mapEmpleados)
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

        contenedor.removeAllViews()
        contenedor.addView(detalleView)
    }


    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
