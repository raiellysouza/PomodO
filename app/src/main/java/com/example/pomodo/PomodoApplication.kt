package com.example.pomodo

import android.app.Application
import com.google.firebase.FirebaseApp

class PomodoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
