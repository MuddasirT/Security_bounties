package com.andy.assistant

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AndyApiClient(private val baseUrl: String) {
    private val client = OkHttpClient.Builder()
        .callTimeout(70, TimeUnit.SECONDS)
        .build()

    fun ask(message: String, callback: (Result<String>) -> Unit) {
        if (baseUrl.contains("YOUR_GATEWAY_HOST")) {
            callback(Result.failure(IllegalStateException("Set the Andy gateway URL in the Gradle property and rebuild.")))
            return
        }
        val payload = JSONObject().put("message", message).toString()
        val body = payload.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/v1/chat")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { callback(Result.failure(e)) }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        callback(Result.failure(IOException("Gateway returned HTTP ${it.code}")))
                        return
                    }
                    try {
                        val reply = JSONObject(it.body?.string().orEmpty()).optString("reply")
                        if (reply.isBlank()) callback(Result.failure(IOException("Gateway returned an empty reply")))
                        else callback(Result.success(reply))
                    } catch (e: Exception) { callback(Result.failure(e)) }
                }
            }
        })
    }
}
