package com.example.wearsmart.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.Text

@Composable
fun ObjectDetail(name: String, onClicker: () -> Unit) {

    /* Devices and Profiles, to be replaced with data obtained from the server */
    val scalingLazyListState: ScalingLazyListState = rememberScalingLazyListState()
    val attributes = listOf("Brightness", "Temperature")

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = scalingLazyListState,
        horizontalAlignment = Alignment.Start,
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(), // Make sure the Box takes up the full width
                contentAlignment = Alignment.Center // Center the text horizontally within the Box
            ) {
                Text(
                    "Lampara Izquierda",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        }
        items(attributes.size) { i ->
            SingleSlider(attributes[i], 0.0f, 1.0f, 0.5f)
        }
        item {
            ColorSlider("Color", 0, 0, 0)
        }
        item{
            Spacer(modifier = Modifier.width(12.dp)) // Space between the buttons
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth(), // Make sure the Box takes up the full width
                contentAlignment = Alignment.Center // Center the text horizontally within the Box
            ) {
                Row {
                    Button(onClick = { /* Handle button 1 click */ },
                        modifier = Modifier.size(ButtonDefaults.SmallButtonSize)) {
                        Text("ON")
                    }
                    Spacer(modifier = Modifier.width(16.dp)) // Space between the buttons
                    Button(onClick = { /* Handle button 2 click */ },
                        modifier = Modifier.size(ButtonDefaults.SmallButtonSize)) {
                        Text("OFF")
                    }
                }
            }
        }
    }
}

@Composable
fun SingleSlider(title: String, min: Float, max: Float, curr: Float) {
    Column {
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, textAlign = TextAlign.Left, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        var value by remember { mutableFloatStateOf(curr) }
        InlineSlider(
            value = value,
            onValueChange = { value = it },
            increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
            decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
            valueRange = min..max,
            steps = 9,
            segmented = false,
            modifier = Modifier.height(40.dp)
        )
    }
}

@Composable
fun ColorSlider(title: String, r: Int, g: Int, b: Int) {
    Column {
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, textAlign = TextAlign.Left, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        var R by remember { mutableFloatStateOf(r.toFloat()) }
        InlineSlider(
            value = R,
            onValueChange = { R = it },
            increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
            decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
            valueRange = 0f..255f,
            steps = 9,
            segmented = false,
            modifier = Modifier.height(40.dp),
            colors = InlineSliderDefaults.colors(
                selectedBarColor = Color(0xccd85252)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        var G by remember { mutableFloatStateOf(g.toFloat()) }
        InlineSlider(
            value = G,
            onValueChange = { G = it },
            increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
            decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
            valueRange = 0f..255f,
            steps = 9,
            segmented = false,
            modifier = Modifier.height(40.dp),
            colors = InlineSliderDefaults.colors(
                selectedBarColor = Color(0xcc4fca3a)
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        var B by remember { mutableFloatStateOf(b.toFloat()) }
        InlineSlider(
            value = B,
            onValueChange = { B = it },
            increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
            decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
            valueRange = 0f..255f,
            steps = 9,
            segmented = false,
            modifier = Modifier.height(40.dp),
            colors = InlineSliderDefaults.colors(
                selectedBarColor = Color(0xcc13139a)
            )
        )
    }
}