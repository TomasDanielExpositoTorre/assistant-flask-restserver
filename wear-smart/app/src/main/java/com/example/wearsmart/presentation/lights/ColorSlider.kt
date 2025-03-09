package com.example.wearsmart.presentation.lights

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.Text


@Composable
fun ColorSlider(r: MutableState<Float>, g: MutableState<Float>, b: MutableState<Float>) {
    Column {
        Text("Color", textAlign = TextAlign.Left, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        InlineSlider(
            value = r.value,
            onValueChange = { value -> r.value = value },
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
        InlineSlider(
            value = g.value,
            onValueChange = { value -> g.value = value },
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
        InlineSlider(
            value = b.value,
            onValueChange = { value -> b.value = value },
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
