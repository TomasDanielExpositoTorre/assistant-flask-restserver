/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.wearsmart.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.wearsmart.presentation.theme.WearSmartTheme
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent { WearSmart() }
    }
}

@Composable
fun WearSmart() {
    WearSmartTheme {
        AppScaffold {
            val navController = rememberSwipeDismissableNavController()
            SwipeDismissableNavHost(navController = navController, startDestination = "home")
            {
                composable("home") {
                    DeviceList(clickFn = { device ->
                        navController.navigate("object/$device")
                    })
                }
                composable(
                    "object/{deviceJson}",
                    arguments = listOf(navArgument("deviceJson") { type = NavType.StringType })
                ) { backStackEntry ->
                    val deviceJsonString = backStackEntry.arguments?.getString("deviceJson") ?: ""
                    val deviceJson = Json.parseToJsonElement(deviceJsonString).jsonObject
                    DeviceDetail(deviceJson)
                }

            }
        }
    }
}

