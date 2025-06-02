package com.kathlg.flowit.ui.management.oficinas

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
import com.kathlg.flowit.R
import com.kathlg.flowit.data.repository.OficinasRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OficinasFragment : Fragment() {

    private val oficinasViewModel: OficinasViewModel by activityViewModels {
        OficinaViewModelFactory(OficinasRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_oficinas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rvListado = view.findViewById<RecyclerView>(R.id.rvListado)
        val etBuscar = view.findViewById<EditText>(R.id.etBuscar)
        val contenedor = view.findViewById<FrameLayout>(R.id.flDetalles)

        val fabExportar = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabExportarCSV)
        fabExportar.setOnClickListener { exportarOficinasCSV() }

        rvListado.layoutManager = LinearLayoutManager(requireContext())
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

        oficinasViewModel.oficinasFiltradas.observe(viewLifecycleOwner) {
            ofAdapter.updateData(it)
        }
        oficinasViewModel.cargarOficinas()

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                oficinasViewModel.buscarPorCodigoFirestore(s.toString())
            }
        })
    }

    private fun exportarOficinasCSV() {
        val builder = StringBuilder()
        val fileName: String
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
        val data = oficinasViewModel.oficinasFiltradas.value

        if (data.isNullOrEmpty()) {
            showToast("No hay oficinas para exportar.")
            return
        }

        // Columnas de ejemplo, cambia/añade según tu modelo
        builder.append("ID,Codigo,Direccion,Ciudad,PuestosTrabajo,PuestosAlumnos,PuestosTeletrabajo\n")
        data.forEach {
            builder.append("${it.id},${it.codigo},${it.direccion},${it.ciudad},${it.puestosTrabajo},${it.puestosAlumnos},${it.puestosTeletrabajo}\n")
        }

        fileName = "Oficinas_Export_$timestamp.csv"
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

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
