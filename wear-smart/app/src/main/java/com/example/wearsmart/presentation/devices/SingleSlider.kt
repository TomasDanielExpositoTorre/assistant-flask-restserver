package com.example.wearsmart.presentation.devices

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.Text

@Composable
fun SingleSlider(title: String, min: Float, max: Float, curr: MutableState<Float>) {
    Column {
        Text(title, textAlign = TextAlign.Left, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        InlineSlider(
            value = curr.value,
            onValueChange = { value -> curr.value = value },
            increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
            decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
            valueRange = min..max,
            steps = 9,
            segmented = false,
            modifier = Modifier.height(40.dp)
        )
    }
}