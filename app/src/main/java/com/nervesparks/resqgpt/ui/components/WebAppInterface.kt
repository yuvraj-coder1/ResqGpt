package com.nervesparks.resqgpt.ui.components

import android.content.Context
import android.webkit.JavascriptInterface
import com.nervesparks.resqgpt.data.GlobalVariables

class WebAppInterface(private val context: Context, private val customPins: String) {

    @JavascriptInterface
    fun getCustomPins(): String{
        val customPins = GlobalVariables.customPins
        return customPins
    }
}