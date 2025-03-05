/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.wearsmart.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TitleCard
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wearsmart.R
import com.example.wearsmart.presentation.theme.WearSmartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("Android")
        }
    }
}

@Composable
fun WearApp(greetingName: String) {
    WearSmartTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    val scalingLazyListState : ScalingLazyListState =
        rememberScalingLazyListState()
    val elementList = listOf("Lampara1", "Lampara2", "Lampara3")
    val profileList = listOf("Perfil1", "Perfil2", "Perfil3")
    ScalingLazyColumn(
        modifier=Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = scalingLazyListState,
        horizontalAlignment = Alignment.Start,
        ) {
        item{
            Text("Devices", textAlign = TextAlign.Left, fontSize=12.sp)
        }
        items(elementList.size) {
            i ->
            Card(
                content = {
                    Text(elementList[i], maxLines=1, overflow=TextOverflow.Ellipsis, fontSize = 12.sp)
                    Text("This is a test", fontWeight = FontWeight.Thin, fontSize = 10.sp)
                },
                onClick = {},
                shape = MaterialTheme.shapes.large,

            )
        }
        item{
            Spacer(modifier=Modifier.height(16.dp))
        }
        item{
            Text("Profiles", textAlign = TextAlign.Left, fontSize=12.sp)
        }
        items(profileList.size) {
                i ->
            Card(
                content = {
                    Text(profileList[i], maxLines=1, overflow=TextOverflow.Ellipsis, fontSize = 12.sp)
                    Text("This is a test", fontWeight = FontWeight.Thin, fontSize = 10.sp)
                },
                onClick = {},
                shape = MaterialTheme.shapes.large,

                )
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}