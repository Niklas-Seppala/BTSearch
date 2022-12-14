package com.asd.btsearch.ui.navigation

import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.asd.btsearch.ui.views.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient

private const val TAG = "Views"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Views(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    permissions: MultiplePermissionsState?,
    scaffoldState: ScaffoldState,
    locationProviderClient: FusedLocationProviderClient
) {
    if (permissions == null) {
        Text(text = "Device does not support BLE")
        return
    }

    NavHost(
        navController = navController,
        startDestination = "${Screen.Home.baseRoute}/{deviceId}"
    ) {
        composable(
            route = "${Screen.Home.baseRoute}/{deviceId}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.IntType
                }
            )
        ) {
            HomeView(
                scaffoldState = scaffoldState,
                navigation = navController,
                permissionsState = permissions,
                locationProviderClient = locationProviderClient,
                deviceId = it.arguments?.getInt("deviceId") ?: -1
            )
        }
        composable(Screen.Stats.baseRoute) {
            StatsView(
                modifier = modifier,
                navigation = navController,
                permissionsState = permissions,
                scaffoldState = scaffoldState
            )
        }
        composable(Screen.Tracing.baseRoute) {
            MeasurementView(scaffoldState = scaffoldState)
        }
        composable(
            route = "${Screen.Photo.baseRoute}/{deviceId}/{deviceMac}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.IntType
                },
                navArgument("deviceMac") {
                    type = NavType.StringType
                }
            )) {
            PhotoView(
                modifier = modifier,
                deviceId = it.arguments?.getInt("deviceId") ?: -1,
            )
        }
    }
}