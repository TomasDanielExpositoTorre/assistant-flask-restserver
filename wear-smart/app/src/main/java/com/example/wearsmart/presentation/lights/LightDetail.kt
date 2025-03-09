package com.example.wearsmart.presentation.lights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import com.example.wearsmart.presentation.NetworkCommunicator
import com.example.wearsmart.presentation.devices.SingleSlider
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun LightDetail(device: JsonObject) {

    val scalingLazyListState: ScalingLazyListState = rememberScalingLazyListState()

    /* Attributes, defaults and references for the page */
    val api = NetworkCommunicator()
    val attributes = device["attributes"]!!.jsonObject.toMutableMap()
    val rgb = attributes.remove("Color")?.jsonArray

    val refs = rememberSaveable { mutableMapOf<String, MutableState<Float>>() }
    val r = remember { mutableFloatStateOf(rgb?.get(0)?.jsonPrimitive?.float ?: 0f) }
    val g = remember { mutableFloatStateOf(rgb?.get(1)?.jsonPrimitive?.float ?: 0f) }
    val b = remember { mutableFloatStateOf(rgb?.get(2)?.jsonPrimitive?.float ?: 0f) }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = scalingLazyListState,
    ) {
        /* Title */
        item {
            Text(
                device["name"]!!.jsonPrimitive.content,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        /* Sliders */
        items(attributes.keys.size) { i ->
            val key = attributes.keys.elementAt(i)
            val attrs = attributes[key]!!.jsonArray
            refs[key] = rememberSaveable { mutableFloatStateOf(attrs[2].jsonPrimitive.float) }
            refs[key]?.let {
                SingleSlider(key, attrs[0].jsonPrimitive.float, attrs[1].jsonPrimitive.float, it)
            }
        }

        /* RGB Slider */
        if (rgb != null) {
            item { ColorSlider(r, g, b) }
        }


        /* Submit buttons */
        item {
            Row {
                Button(
                    onClick = {
                    api.post(device["id"]!!.jsonPrimitive.content, refs, rgb, r, g, b)
                }, modifier = Modifier.size(ButtonDefaults.SmallButtonSize), content = {
                    Text("ON", color = Color.White)
                }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xcc4fca3a)
                )
                )
                Spacer(modifier = Modifier.width(16.dp)) // Space between the buttons
                Button(
                    onClick = {
                    api.post(device["id"]!!.jsonPrimitive.content)
                }, modifier = Modifier.size(ButtonDefaults.SmallButtonSize), content = {
                    Text("OFF", color = Color.White)
                }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xccd85252)
                )
                )
            }
        }
    }
}

