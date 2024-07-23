package com.example.mosisproject

import android.app.Application

class MOSISProject:Application (){
    init {
        instance=this
    }
    companion object{
        private var instance:MOSISProject?=null
        fun applicationContext():MOSISProject{
            return instance as MOSISProject
        }
    }

    override fun onCreate() {
        super.onCreate()
        LocationWizard()
    }
}