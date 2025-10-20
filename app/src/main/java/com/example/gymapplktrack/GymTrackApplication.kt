package com.example.gymapplktrack

import android.app.Application

class GymTrackApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
