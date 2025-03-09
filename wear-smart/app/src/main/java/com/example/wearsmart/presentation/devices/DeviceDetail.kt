package com.example.wearsmart.presentation.devices

import androidx.compose.runtime.Composable
import com.example.wearsmart.presentation.lights.LightDetail
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun DeviceDetail(device: JsonObject) {
    when (val type = device["type"]!!.jsonPrimitive.content){
        "light" -> LightDetail(device)
        else -> throw IllegalArgumentException("Unknown type: $type")
    }
}