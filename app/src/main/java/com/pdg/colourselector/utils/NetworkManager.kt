package com.pdg.colourselector.utils

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.pdg.colourselector.Constants


class NetworkManager {
    fun hasNetworkAccess(context: Context): Boolean {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnected
        } catch (e: Exception) {
            Log.e(Constants.TAG,"Error: ${e.message}")
            e.printStackTrace()
            return false
        }

    }
}