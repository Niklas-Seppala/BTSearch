package com.asd.btsearch.ui.navigation

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.asd.btsearch.ui.views.HomeView
import com.asd.btsearch.ui.views.InfoView
import com.asd.btsearch.ui.views.SettingsView
import com.asd.btsearch.ui.views.StatsView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient

private const val TAG = "Views"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Views(modifier: Modifier = Modifier,
    navController: NavHostController,
  permissions: MultiplePermissionsState?,
  locationProviderClient: FusedLocationProviderClient) {

    if (permissions == null) {  // TODO: Error page?
        Text(text = "Device does not support BLE")
        return
    }

    NavHost(navController = navController, startDestination = Screen.Home.baseRoute) {
        composable(Screen.Home.baseRoute) {
            HomeView(
                navigation = navController,
                permissionsState = permissions,
                locationProviderClient = locationProviderClient
            )
        }
        composable(Screen.Stats.baseRoute) {
            StatsView(modifier = modifier, navigation = navController, permissionsState = permissions)
        }
        composable(Screen.Info.baseRoute) {
            InfoView(navigation = navController, permissionsState = permissions)
        }
        composable(Screen.Settings.baseRoute) {
            SettingsView(navigation = navController, permissionsState = permissions)
        }
    }
}