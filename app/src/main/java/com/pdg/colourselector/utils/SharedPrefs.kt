package com.pdg.colourselector.utils

import android.content.Context
import android.preference.PreferenceManager
import com.pdg.colourselector.Constants

class SharedPref {
    companion object {

        fun getToken(context: Context): String {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getString(Constants.TOKEN_AUTH, "")
        }

        fun setToken(token: String, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putString(Constants.TOKEN_AUTH, token)
            editor.apply()
        }

        fun getStorageID(context: Context): Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getInt(Constants.STORAGE_ID, -1)
        }

        fun setStorageID(id: Int, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putInt(Constants.STORAGE_ID, id)
            editor.apply()
        }
    }
}