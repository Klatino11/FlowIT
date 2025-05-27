package com.kathlg.flowit

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigationrail.NavigationRailView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val navigationRail = findViewById<NavigationRailView>(R.id.navigationRail)
        navigationRail.menu.findItem(R.id.nav_dispositivos).isChecked = false
        navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dispositivos -> showToast("Dispositivos")
                R.id.nav_empleados -> showToast("Empleados")
                R.id.nav_oficinas -> showToast("Oficinas")
                R.id.nav_departamentos -> showToast("Departamentos")
            }
            true
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
