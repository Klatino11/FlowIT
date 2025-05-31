package com.kathlg.flowit

import android.app.Application
import com.google.firebase.FirebaseApp

class FlowItApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Solo inicializamos el FirebaseApp "default"
        FirebaseApp.initializeApp(this)
    }
}
