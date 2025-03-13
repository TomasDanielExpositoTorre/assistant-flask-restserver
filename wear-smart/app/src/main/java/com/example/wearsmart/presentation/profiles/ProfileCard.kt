package com.example.wearsmart.presentation.profiles

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
import com.example.wearsmart.presentation.NetworkCommunicator
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun ProfileCard(profile: JsonObject, clickFn: (Boolean) -> Unit) {
    val title = profile["name"]!!.jsonPrimitive.content
    val devices = profile["devices"]!!.jsonArray.joinToString(", ") { it.jsonPrimitive.content }
    val api = NetworkCommunicator()

    Card(
        onClick = {
            api.postProfile(title)
        },
        shape = MaterialTheme.shapes.large,
    ) {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = devices, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Thin, fontSize = 10.sp
            )
        }
}
