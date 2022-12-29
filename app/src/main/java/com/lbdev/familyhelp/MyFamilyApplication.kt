package com.lbdev.familyhelp

import android.app.Application

open class MyFamilyApplication:Application() {

    override fun onCreate() {
        super.onCreate()

        SharedPref.init(this)
    }
}