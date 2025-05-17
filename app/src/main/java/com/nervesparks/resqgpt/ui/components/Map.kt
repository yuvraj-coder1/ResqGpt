package com.nervesparks.resqgpt.ui.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nervesparks.resqgpt.data.GlobalVariables


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapContent(url: String) {
    Column(Modifier.padding(top = 32.dp)) {
        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.setGeolocationEnabled(true)

                webChromeClient = object : WebChromeClient() {
                    override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                        callback.invoke(origin, true, false)
                    }
                }

                // Add the JavaScript interface
                Log.d("TAG", "what are we sending: ${GlobalVariables.customPins}")
                addJavascriptInterface(WebAppInterface(it.applicationContext, GlobalVariables.customPins), "Android")

                loadUrl(url)
            }
        }, update = {
            it.loadUrl(url)
        })
    }
}