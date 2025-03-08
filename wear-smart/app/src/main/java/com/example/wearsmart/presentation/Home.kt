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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


@Composable
fun Home(clickFn: (JsonObject) -> Unit) {

    /* Devices and Profiles, to be replaced with data obtained from the server */
    val scalingLazyListState: ScalingLazyListState = rememberScalingLazyListState()
    val profiles = listOf("Perfil1", "Perfil2", "Perfil3")
    val devices = remember { mutableStateOf(JsonArray(emptyList())) }

    LaunchedEffect(Unit) {
        devices.value = fetchData()!!
    }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = scalingLazyListState,
        horizontalAlignment = Alignment.Start,
    ) {
        /* Device List, extracted from the flask API */
        item { Text("Devices", textAlign = TextAlign.Left, fontSize = 12.sp) }
        items(devices.value.size) { i -> DeviceCard(devices.value[i].jsonObject, clickFn) }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        /* Profile List */
        item { Text("Profiles", textAlign = TextAlign.Left, fontSize = 12.sp) }
        items(profiles.size) { i -> ProfileCard(profiles[i], {}) }
    }
}

@Composable
fun DeviceCard(device: JsonObject, clickFn: (JsonObject) -> Unit) {
    /* Title: Device friendly name */
    val title = device["name"]!!.jsonPrimitive.content

    /* Subtitle: modifiable attributes */
    val attrs = device["attributes"]!!.jsonObject.keys.joinToString(", ")
    val rgb = if (device["rgb"]?.jsonArray != null) ", Color" else ""

    Card(
        onClick = { clickFn(device) },
        shape = MaterialTheme.shapes.large,
    ) {
        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            attrs + rgb,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Thin,
            fontSize = 10.sp
        )
    }
}


@Composable
fun ProfileCard(title: String, clickFn: (Boolean) -> Unit) {
    ToggleChip(
        checked = false,
        label = {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
        },
        onCheckedChange = clickFn,
        toggleControl = {
            Switch(
                checked = false,
                enabled = true,
            )
        },
        modifier = Modifier.fillMaxSize()
    )
}

suspend fun fetchData(): JsonArray? {
    val client = OkHttpClient()
    val url = "https://192.168.1.64:8000/devices"
    val request = Request.Builder().url(url).build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Json.parseToJsonElement(response.body!!.string()).jsonArray
            } else {
                null
            }
        } catch (e: Exception) {
            println(e)
            null
        }
    }
}