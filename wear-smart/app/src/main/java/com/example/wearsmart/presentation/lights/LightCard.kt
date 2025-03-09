package com.example.wearsmart.presentation.lights

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun LightCard(device: JsonObject, clickFn: (JsonObject) -> Unit) {
    val title = device["name"]!!.jsonPrimitive.content
    val attrs = device["attributes"]!!.jsonObject.keys.joinToString(", ")

    Card(
        onClick = { clickFn(device) },
        shape = MaterialTheme.shapes.large,
    ) {
        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            attrs, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Thin, fontSize = 10.sp
        )
    }
}