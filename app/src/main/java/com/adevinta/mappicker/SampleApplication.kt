package com.adevinta.mappicker

import androidx.multidex.MultiDexApplication
import com.google.android.gms.maps.MapsInitializer

class SampleApplication : MultiDexApplication(){
    override fun onCreate() {
        super.onCreate()
        MapsInitializer.initialize(this)
    }
}
