package com.nervesparks.resqgpt.receiver

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.nervesparks.resqgpt.data.resendQueuedSms

fun monitorNetwork(context: Context) {
    val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d("INTERNET", "Network is available")
            resendQueuedSms(context) // <== This will trigger your SMS sending
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d("INTERNET", "Network is lost")
        }
    }

    connectivityManager.registerDefaultNetworkCallback(networkCallback)
}

fun Context.isNetworkAvailable(): Boolean {
    val manager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
    } else {
        try {
            val activeNetworkInfo = manager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
//                showLog("Network is available : true", "NETWORK STATUS")
                return true
            }
        } catch (e: Exception) {

        }
    }
    return false
}