package com.nervesparks.resqgpt.data

import android.content.Context
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nervesparks.resqgpt.model.EmergencyContact
import com.nervesparks.resqgpt.receiver.isNetworkAvailable

fun sendSmsDirectly(context: Context, to: String, message: String) {
    if (!context.isNetworkAvailable()) {
        saveSmsToQueue(context, QueuedSms(to, message))
        println("No network. SMS queued for later.")
        return
    }

    val client = OkHttpClient()
    val formBody = FormBody.Builder()
        .add("To", to)
        .add("From", PHONE_NUMBER)
        .add("Body", message)
        .build()

    val credential = Credentials.basic(ACCOUNT_SID, AUTH_TOKEN)
    val request = Request.Builder()
        .url("https://api.twilio.com/2010-04-01/Accounts/$ACCOUNT_SID/Messages.json")
        .post(formBody)
        .header("Authorization", credential)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Failed to send SMS: ${e.message}")
            saveSmsToQueue(context, QueuedSms(to, message)) // re-queue
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
            println("Response code: ${response.code}")
            println("Response body: $body")
        }
    })
}


fun saveSmsToQueue(context: Context, sms: QueuedSms) {
    val sharedPrefs = context.getSharedPreferences("sms_queue", Context.MODE_PRIVATE)
    val gson = Gson()
    val current = sharedPrefs.getString("queue", "[]")
    val queue = gson.fromJson(current, Array<QueuedSms>::class.java).toMutableList()
    queue.add(sms)
    sharedPrefs.edit { putString("queue", gson.toJson(queue)) }
}

fun saveEmergencyContacts(context: Context, contacts: List<EmergencyContact>) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = gson.toJson(contacts)
    prefs.edit { putString("emergency_contacts", json) }
}

fun loadEmergencyContacts(context: Context): List<EmergencyContact> {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = prefs.getString("emergency_contacts", null) ?: return emptyList()
    val type = object : TypeToken<List<EmergencyContact>>() {}.type
    return gson.fromJson(json, type)
}


data class QueuedSms(
    val to: String,
    val message: String
)

fun resendQueuedSms(context: Context) {
    val sharedPreferences = context.getSharedPreferences("sms_queue", Context.MODE_PRIVATE)
    val gson = Gson()
    val currentQueueJson = sharedPreferences.getString("queue", "[]")
    val queue = gson.fromJson(currentQueueJson, Array<QueuedSms>::class.java).toMutableList()

    for (sms in queue) {
        sendSmsDirectly(context,sms.to, sms.message) // This uses your original sendSmsDirectly()
    }

    sharedPreferences.edit { remove("queue") }
}

