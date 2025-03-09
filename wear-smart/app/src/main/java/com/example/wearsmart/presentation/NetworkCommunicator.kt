package com.example.wearsmart.presentation

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class NetworkCommunicator() {
    private val client = OkHttpClient()
    private val url: String = "https://glados.local:8000/devices";

    /**
     * Fetch list of devices from the API.
     *
     * @return JsonArray with information for each device, or single-element
     * array.
     */
    suspend fun get(): JsonArray {
        val request = Request.Builder().url(url).build()
        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Json.parseToJsonElement(response.body!!.string()).jsonArray
                } else {
                    JsonArray(listOf(JsonObject(mapOf(
                        "name" to JsonPrimitive("Unavailable"),
                        "attributes" to JsonObject(
                            mapOf("Error ${response.code}" to JsonPrimitive(true)))))))
                }
            } catch (e: Exception) {
                println(e)
                JsonArray(listOf(JsonObject(mapOf(
                    "name" to JsonPrimitive("Unavailable"),
                    "attributes" to JsonObject(
                        mapOf("API Unreachable" to JsonPrimitive(true)))))))
            }
        }
    }

    /**
     * Post request wrapper for a `light` device type when turned on
     */
    fun post(
        entityId: String, refs: MutableMap<String, MutableState<Float>>, rgb: JsonArray?,
        r: MutableState<Float>, g: MutableState<Float>, b: MutableState<Float>
    ) {

        val data = mutableMapOf<String, Any>(
            "entity_id" to entityId,
            "off" to false
        )

        for ((key, state) in refs) {
            data[key] = state.value
        }

        if (rgb != null) {
            data["rgb"] = listOf(r.value.toInt(), g.value.toInt(), b.value.toInt())
        }

        this.post(toJson(data));
    }


    /**
     * Post request wrapper for a `light` device type when turned off
     */
    fun post(entityId: String) {

        val data = mutableMapOf<String, Any>(
            "entity_id" to entityId,
            "off" to true
        )

        this.post(toJson(data));
    }

    /**
     * Post request to API
     */
    private fun post(data: JsonObject) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonInputString = data.toString()

                val body = jsonInputString.toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("https://glados.local:8000/devices")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()

                val response: Response = client.newCall(request).execute()
                println("Response Code: ${response.code}")
                println("Response Body: ${response.body?.string()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun toJson(data: Map<String, Any>): JsonObject {
        return JsonObject(data.mapValues { entry ->
            when (val value = entry.value) {
                is String -> JsonPrimitive(value)
                is Boolean -> JsonPrimitive(value)
                is Int -> JsonPrimitive(value)
                is Float -> JsonPrimitive(value)
                is List<*> -> {
                    if (value.all { it is Int }) {
                        JsonArray(value.map { JsonPrimitive(it as Int) })
                    } else {
                        throw IllegalArgumentException("All items in the list must be integers")
                    }
                }

                else -> throw IllegalArgumentException("Unsupported type")
            }
        })

    }

}