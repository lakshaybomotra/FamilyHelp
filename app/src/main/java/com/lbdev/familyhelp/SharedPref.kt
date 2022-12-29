package com.lbdev.familyhelp

import android.content.Context
import android.content.SharedPreferences

object SharedPref {

    private const val NAME = "FamilyHelp"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    fun putBoolean(key:String,value: Boolean){
        preferences.edit().putBoolean(key,value).apply()
    }

    fun getBoolean(key:String):Boolean {
        return preferences.getBoolean(key,false)
    }

}