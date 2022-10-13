package com.asd.btsearch.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.asd.btsearch.repository.DeviceDatabase
import com.asd.btsearch.repository.DeviceEntity
import com.asd.btsearch.ui.events.Permissions
import com.asd.btsearch.ui.navigation.Screen
import com.asd.btsearch.ui.theme.BTSearchTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "HomeView"

class MapViewModel(context: Context) : ViewModel() {
    private val repo = DeviceDatabase.get(context).deviceDao()
    val devices = repo.getEntries()

    suspend fun tryGetDevice(deviceId: Int): DeviceEntity? {
        return viewModelScope.async(Dispatchers.IO) {
            repo.getDevice(deviceId)
        }.await()
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeView(
    scaffoldState: ScaffoldState,
    mapViewModel: MapViewModel = MapViewModel(LocalContext.current),
    navigation: NavHostController,
    permissionsState: MultiplePermissionsState,
    locationProviderClient: FusedLocationProviderClient,
    deviceId: Int
) {
    if (!Permissions.hasLocation(permissionsState)) {
        return
    }

    val devices = mapViewModel.devices.observeAsState()
    var location by remember { mutableStateOf<Location?>(null) }
    var device by remember { mutableStateOf<DeviceEntity?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(deviceId) {
        device = mapViewModel.tryGetDevice(deviceId)
    }

    LaunchedEffect(Unit) {
        Log.d(TAG, "Setting location callback")
        locationProviderClient.lastLocation.addOnSuccessListener {
            if (location == null) location = it
            else if (location!!.distanceTo(it) > 2f) {
                location = it
            }
        }
        val locationRequest = LocationRequest.create()
            .setInterval(1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)

        locationProviderClient.requestLocationUpdates(
            locationRequest, object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.locations.forEach {
                        if (location == null) location = it
                        else if (location!!.distanceTo(it) > 2f) {
                            location = it
                        }
                    }
                }
            }, Looper.getMainLooper()
        )
    }
    Map(
        onDeviceClicked = {
            scope.launch {
                val res = scaffoldState.snackbarHostState.showSnackbar(
                    it.mac, "Go to details", SnackbarDuration.Long
                )
                when (res) {
                    SnackbarResult.Dismissed -> {}
                    SnackbarResult.ActionPerformed -> {
                        navigation.navigate(Screen.Stats.baseRoute)
                    }
                }
            }
        },
        location = location,
        selectedDevice = device,
        devices = devices.value ?: listOf()
    )
}

@Composable
fun ContentCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
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
