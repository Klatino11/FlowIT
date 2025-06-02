package com.kathlg.flowit.ui.management.dispositivos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kathlg.flowit.R
import com.kathlg.flowit.data.model.Dispositivo
import com.kathlg.flowit.data.repository.DispositivosRepository
import com.kathlg.flowit.data.repository.EmpleadosRepository
import com.kathlg.flowit.ui.management.empleados.EmpleadoViewModelFactory
import com.kathlg.flowit.ui.management.empleados.EmpleadosViewModel
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

    private var mapEmpleados: Map<String, String> = emptyMap()
    private lateinit var dispAdapter: DispositivoAdapter

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

        // Inicializa el adapter vacío
        dispAdapter = DispositivoAdapter(emptyList(), mapEmpleados) { dispositivo ->
            mostrarDetalleDispositivo(dispositivo, contenedor)
        }
        rvListado.layoutManager = LinearLayoutManager(requireContext())
        rvListado.adapter = dispAdapter

        // Observa empleados y dispositivos, y actualiza el adapter en cada cambio
        empleadosViewModel.empleadosFiltrados.observe(viewLifecycleOwner) { listaEmpleados ->
            mapEmpleados = listaEmpleados.associate { it.codigo to it.nombre }
            // Actualiza también la lista de dispositivos con el nuevo mapa
            val dispositivos = dispositivosViewModel.devices.value ?: emptyList()
            dispAdapter.updateData(dispositivos, mapEmpleados)
        }
        dispositivosViewModel.devices.observe(viewLifecycleOwner) { dispositivos ->
            dispAdapter.updateData(dispositivos, mapEmpleados)
        }

        // Carga datos
        empleadosViewModel.cargarEmpleados()
        dispositivosViewModel.loadDevices()

        // Si tienes búsqueda, añade el filtro aquí (¡adáptalo a tu ViewModel si usas filtro por código!)
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // dispositivosViewModel.buscarPorCodigo(s.toString())
                // Si tu ViewModel aún no tiene método para filtrar, lo puedes implementar después
                showToast("Función búsqueda de dispositivos por código aún no implementada")
            }
        })
    }

    private fun exportarDispositivosCSV() {
        val builder = StringBuilder()
        val fileName: String
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
        val data = dispositivosViewModel.devices.value

        if (data.isNullOrEmpty()) {
            showToast("No hay dispositivos para exportar.")
            return
        }
        // Ajusta los campos según lo que quieras exportar
        builder.append("ID,Código,Nombre,Tipo,RAM,Modelo,Marca,NúmeroSerie,Precio,Empleado\n")
        data.forEach {
            //builder.append("${it.id},${it.codigo},${it.nombre},${it.tipo},${it.ramGb},${it.modelo},${it.marca},${it.numeroSerie},${it.precio},${it.codigoEmpleado}\n")
        }

        fileName = "Dispositivos_Export_$timestamp.csv"
        try {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            file.writeText(builder.toString())
            showToast("CSV exportado en: ${file.absolutePath}")
        } catch (e: Exception) {
            showToast("Error al exportar CSV")
            e.printStackTrace()
        }
    }


    private fun mostrarDetalleDispositivo(dispositivo: Dispositivo, contenedor: FrameLayout) {
        val detalleView = layoutInflater.inflate(R.layout.detalle_dispositivo, null)

        // Setea los campos básicos
        detalleView.findViewById<TextView>(R.id.tvDetalleNombre).text = dispositivo.nombre
        detalleView.findViewById<TextView>(R.id.tvDetalleTipo).text = "Tipo: ${dispositivo.tipo}"
        detalleView.findViewById<TextView>(R.id.tvDetalleRam).text = "RAM: ${dispositivo.ramGb} GB"
        detalleView.findViewById<TextView>(R.id.tvDetalleModelo).text = "Modelo: ${dispositivo.modelo}"
        detalleView.findViewById<TextView>(R.id.tvDetalleMarca).text = "Marca: ${dispositivo.marca}"
        detalleView.findViewById<TextView>(R.id.tvDetalleNumSerie).text = "Nº Serie: ${dispositivo.numeroSerie}"
        detalleView.findViewById<TextView>(R.id.tvDetallePrecio).text = "Precio: %.2f €".format(dispositivo.precio)

        // Empleado asignado
        detalleView.findViewById<TextView>(R.id.tvDetalleEmpleado).text = "Empleado: ${dispositivo.codigoEmpleado}"

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
