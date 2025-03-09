package com.example.wearsmart.presentation.profiles

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip

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
