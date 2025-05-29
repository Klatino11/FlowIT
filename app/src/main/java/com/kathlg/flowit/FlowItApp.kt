// FlowItApp.kt
package com.kathlg.flowit

import android.app.Application
import com.google.firebase.FirebaseApp

class FlowItApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa Firebase con la configuración de google-services.json
        FirebaseApp.initializeApp(this)
    }
}