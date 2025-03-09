package com.example.wearsmart.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.wearsmart.presentation.devices.DeviceDetail
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
            SwipeDismissableNavHost(navController = navController, startDestination = "list") {
                composable("list") {
                    DeviceList(clickFn = { device ->
                        navController.navigate("detail/$device")
                    })
                }
                composable(
                    "detail/{deviceJson}",
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

