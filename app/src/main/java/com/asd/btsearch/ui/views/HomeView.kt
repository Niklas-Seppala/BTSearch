package com.asd.btsearch.ui.views

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.asd.btsearch.ui.events.Permissions
import com.asd.btsearch.ui.theme.BTSearchTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.gms.location.*

private const val TAG = "HomeView"

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeView(
        navigation: NavHostController,
        permissionsState: MultiplePermissionsState,
        locationProviderClient: FusedLocationProviderClient) {

    if (!Permissions.hasLocation(permissionsState)) {
        return
    }

    var location by remember { mutableStateOf<Location?>(null) }

    LaunchedEffect(true) {
        Log.d(TAG, "Setting location callback")
        locationProviderClient.lastLocation.addOnSuccessListener { location = it }
        val locationRequest = LocationRequest.create()
            .setInterval(1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)

        locationProviderClient.requestLocationUpdates(
            locationRequest, object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.locations.forEach { location = it }
                }
            }, Looper.getMainLooper()
        )
    }
    Map(location = location)
}

@Composable
fun ContentCard(modifier: Modifier = Modifier, content: @Composable ()->Unit) {
    Card(modifier = modifier.then(Modifier.padding(4.dp)), elevation = 2.dp) {
        Surface(modifier = Modifier.padding(8.dp)) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun ContentCardPreview() {
    BTSearchTheme {
        Surface(modifier = Modifier.padding(8.dp)) {
            ContentCard {
                Text(text = "Hello")
            }
        }
    }
}
