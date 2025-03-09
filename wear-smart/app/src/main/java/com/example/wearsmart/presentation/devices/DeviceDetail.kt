package com.example.wearsmart.presentation.devices

import androidx.compose.runtime.Composable
import com.example.wearsmart.presentation.lights.LightDetail
import kotlinx.serialization.json.JsonObject

@Composable
fun DeviceDetail(device: JsonObject) {
    LightDetail(device)
}


