package com.nervesparks.resqgpt

import android.app.Application
import com.nervesparks.resqgpt.receiver.monitorNetwork

class ResQGptApp : Application() {
    companion object{
        lateinit var instance: ResQGptApp
            private set
    }

    override fun onCreate(){
        super.onCreate()
        instance = this
        monitorNetwork(this)
    }
}