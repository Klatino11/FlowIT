package com.kathlg.flowit.ui.management.departamentos

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
import com.kathlg.flowit.data.repository.DepartamentosRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DepartamentosFragment : Fragment() {

    private val departamentosViewModel: DepartamentosViewModel by activityViewModels {
        DepartamentosViewModelFactory(DepartamentosRepository())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_departamentos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rvListado = view.findViewById<RecyclerView>(R.id.rvListado)
        val etBuscar = view.findViewById<EditText>(R.id.etBuscar)
        val contenedor = view.findViewById<FrameLayout>(R.id.flDetalles)

        val fabExportar = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabExportarCSV)
        fabExportar.setOnClickListener {
            exportarDepartamentosCSV()
        }


        rvListado.layoutManager = LinearLayoutManager(requireContext())
        val deptAdapter = DepartamentoAdapter(emptyList()) { departamento ->
            val detalleView = layoutInflater.inflate(R.layout.detalle_departamento, null)
            detalleView.findViewById<TextView>(R.id.tvDetalleCodigo).text = departamento.codigo
            detalleView.findViewById<TextView>(R.id.tvDetalleNombre).text = departamento.nombre
            contenedor.removeAllViews()
            contenedor.addView(detalleView)
        }
        rvListado.adapter = deptAdapter

        departamentosViewModel.departamentosFiltrados.observe(viewLifecycleOwner) {
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

    private fun exportarDepartamentosCSV() {
        val builder = StringBuilder()
        val fileName: String
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
        val data = departamentosViewModel.departamentosFiltrados.value

        if (data.isNullOrEmpty()) {
            showToast("No hay departamentos para exportar.")
            return
        }

        // Ajusta columnas según tus necesidades
        builder.append("ID,Codigo,Nombre\n")
        data.forEach {
            builder.append("${it.id},${it.codigo},${it.nombre}\n")
        }

        fileName = "Departamentos_Export_$timestamp.csv"
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
