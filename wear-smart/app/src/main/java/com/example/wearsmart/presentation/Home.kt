package com.example.wearsmart.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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


@Composable
fun Home(onClicker: () -> Unit) {

    /* Devices and Profiles, to be replaced with data obtained from the server */
    val scalingLazyListState: ScalingLazyListState = rememberScalingLazyListState()
    val devices = listOf("Lampara1", "Lampara2", "Lampara3")
    val profiles = listOf("Perfil1", "Perfil2", "Perfil3")

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = scalingLazyListState,
        horizontalAlignment = Alignment.Start,
    ) {
        item { Text("Devices", textAlign = TextAlign.Left, fontSize = 12.sp) }
        items(devices.size) { i -> DeviceCard(devices[i], "This is a test", onClicker) }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item { Text("Profiles", textAlign = TextAlign.Left, fontSize = 12.sp) }
        items(profiles.size) { i -> ProfileCard(profiles[i], {}) }
    }
}

@Composable
fun DeviceCard(title: String, subtitle: String, clickFn: () -> Unit) {
    Card(
        onClick = clickFn,
        shape = MaterialTheme.shapes.large,
    ) {
        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
        Text(subtitle, fontWeight = FontWeight.Thin, fontSize = 10.sp)
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