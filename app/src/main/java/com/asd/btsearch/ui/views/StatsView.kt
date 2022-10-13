package com.asd.btsearch.ui.views

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.asd.btsearch.repository.DeviceDatabase
import com.asd.btsearch.repository.DeviceEntity
import com.asd.btsearch.ui.navigation.Screen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "StatsView"

class DevicesViewModel(context: Context) : ViewModel() {
    private val db = DeviceDatabase.get(context).deviceDao()
    val devices = db.getEntries()

    fun DEBUG_ADD_DUMMY_ENTRY() {
        viewModelScope.launch(Dispatchers.IO) {
            db.insertEntry(
                DeviceEntity(
                    timestamp = System.currentTimeMillis() / 1000,
                    name = "Samsung TV",
                    mac = "00-B0-D0-63-C2-26",
                    lat = 60.233282,
                    lon = 24.835913,
                    isConnectable = true
                )
            )
        }
    }

    fun attachPhoto(deviceId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db.toggleImage(deviceId, true)
        }
    }

    fun delete(device: DeviceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.deleteEntry(device.id);
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StatsView(
    scaffoldState: ScaffoldState,
    modifier: Modifier = Modifier,
    navigation: NavHostController, permissionsState: MultiplePermissionsState,
    devicesViewModel: DevicesViewModel = DevicesViewModel(LocalContext.current)
) {
    val devices = devicesViewModel.devices.observeAsState(listOf())

    Column(modifier.padding(bottom = 30.dp)) {
        Button(modifier = Modifier.padding(start =  8.dp),
            onClick = { devicesViewModel.DEBUG_ADD_DUMMY_ENTRY() }) {
            Text(text = "Add debug entry")
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items = devices.value) {
                DeviceCard(
                    device = it,
                    deviceName = it.name,
                    deviceMac = it.mac,
                    deviceTimestamp = it.timestamp,
                    deviceLat = it.lat,
                    deviceLon = it.lon,
                    onDelete = { devicesViewModel.delete(it) },
                    onPhotoSuccess =  {
                        scaffoldState.snackbarHostState.showSnackbar("Image saved");
                        devicesViewModel.attachPhoto(it.id)
                    },
                    onPhotoError = {
                        scaffoldState.snackbarHostState.showSnackbar("Could not attach image");
                    },
                    onPhotoClick = {
                        Log.d(TAG, "navigate to photo")
                        navigation.navigate(Screen.Photo.withArgs("${it.id}", it.mac))
                    },
                    onJumpToLocation = {
                        navigation.navigate(Screen.Home.withArgs("${it.id}"))
                    })
            }
        }
    }
}
