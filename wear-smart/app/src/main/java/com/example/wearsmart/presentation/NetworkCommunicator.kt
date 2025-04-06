package com.example.wearsmart.presentation

import android.util.Log
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.net.InetAddress
import java.net.UnknownHostException

class NetworkCommunicator {

    var client : OkHttpClient
    companion object {
        var url : String = "https://glados.local:8000";
    }

    init {


        val dns = object : Dns {
            private val cache = mutableMapOf<String, List<InetAddress>>()

            @Throws(UnknownHostException::class)
            override fun lookup(hostname: String): List<InetAddress> {
                cache[hostname]?.let {
                    return it
                }

                println("Custom DNS lookup for: $hostname")
                val result = listOf(InetAddress.getByName(hostname))
                cache[hostname] = result
                return result
            }
        }
        client = OkHttpClient.Builder().dns(dns).build()
    }
    /**
     * Fetch list of devices from the API.
     *
     * @return JsonArray with information for each device, or single-element
     * array.
     */
    suspend fun get(endpoint: String): JsonArray {
        val request = Request.Builder().url("${url}/${endpoint}").build()
        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Json.parseToJsonElement(response.body!!.string()).jsonArray
                } else {
                    JsonArray(
                        listOf(
                            JsonObject(
                                mapOf(
                                    "name" to JsonPrimitive("Unavailable"),
                                    "type" to JsonPrimitive("light"),
                                    "attributes" to JsonObject(
                                        mapOf("Error ${response.code}" to JsonPrimitive(true))
                                    ),
                                    "devices" to JsonArray(
                                        listOf(
                                            JsonPrimitive("API Unreachable"),
                                            JsonPrimitive("Check connection")
                                        )
                                    )
                                )
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("WearSmartERR", "Exception occurred: $e", e)
                e.printStackTrace()
                JsonArray(
                    listOf(
                        JsonObject(
                            mapOf(
                                "name" to JsonPrimitive("Unavailable"),
                                "type" to JsonPrimitive("light"),
                                "attributes" to JsonObject(
                                    mapOf("API Unreachable" to JsonPrimitive(true))
                                ),
                                "devices" to JsonArray(
                                    listOf(
                                        JsonPrimitive("API Unreachable"),
                                        JsonPrimitive("Check connection")
                                    )
                                )
                            )
                        )
                    )
                )
            }
        }
    }

    suspend fun getDevices(): JsonArray {
        return get("devices")
    }

    suspend fun getProfiles(): JsonArray {
        return get("profiles")
    }

    /**
     * Post request wrapper for a `light` device type when turned on
     */
    fun post(
        entityId: String,
        refs: MutableMap<String, MutableState<Float>>,
        rgb: JsonArray?,
        r: MutableState<Float>,
        g: MutableState<Float>,
        b: MutableState<Float>
    ) {

        val data = mutableMapOf<String, Any>(
            "entity_id" to entityId, "off" to false
        )

        for ((key, state) in refs) {
            data[key] = state.value
        }

        if (rgb != null) {
            data["rgb"] = listOf(r.value.toInt(), g.value.toInt(), b.value.toInt())
        }

        this.post(toJson(data), "devices")
    }


    /**
     * Post request wrapper for a `light` device type when turned off
     */
    fun post(entityId: String) {

        val data = mutableMapOf<String, Any>(
            "entity_id" to entityId, "off" to true
        )

        this.post(toJson(data), "devices")
    }

    /**
     * Post request wrapper for a `light` device type when turned off
     */
    fun postProfile(profileName: String) {

        val data = mutableMapOf<String, Any>(
            "profile" to profileName
        )

        this.post(toJson(data), "profiles")
    }

    /**
     * Post request to API
     */
    private fun post(data: JsonObject, endpoint: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonInputString = data.toString()

                val body = jsonInputString.toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder().url("${url}/${endpoint}")
                    .addHeader("Content-Type", "application/json").post(body).build()

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