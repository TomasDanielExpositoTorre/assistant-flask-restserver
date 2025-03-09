package com.example.wearsmart.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Text
import com.example.wearsmart.presentation.lights.LightCard
import com.example.wearsmart.presentation.profiles.ProfileCard
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


@Composable
fun DeviceList(clickFn: (JsonObject) -> Unit) {

    val scalingLazyListState: ScalingLazyListState = rememberScalingLazyListState()
    val profiles = listOf("Perfil1", "Perfil2", "Perfil3")
    val devices = remember { mutableStateOf(JsonArray(emptyList())) }
    val api = NetworkCommunicator()

    LaunchedEffect(Unit) {
        devices.value = api.get()
    }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = scalingLazyListState,
        horizontalAlignment = Alignment.Start,
    ) {
        /* Device List */
        item { Text("Devices", textAlign = TextAlign.Left, fontSize = 12.sp) }
        items(devices.value.size) { i ->
            when(val type = devices.value[i].jsonObject["type"]!!.jsonPrimitive.content) {
                "light" -> LightCard(devices.value[i].jsonObject, clickFn)
                else -> throw IllegalArgumentException("Unknown type: $type")
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        /* Profile List */
        item { Text("Profiles", textAlign = TextAlign.Left, fontSize = 12.sp) }
        items(profiles.size) { i -> ProfileCard(profiles[i], {}) }
    }
}


