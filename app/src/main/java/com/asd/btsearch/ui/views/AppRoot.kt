package com.asd.btsearch.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.asd.btsearch.R
import com.asd.btsearch.ui.events.BottomBarClickHandler
import com.asd.btsearch.ui.events.rememberPermissionState
import com.asd.btsearch.ui.navigation.Navigable
import com.asd.btsearch.ui.navigation.Views
import com.asd.btsearch.ui.theme.Orange200
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.FusedLocationProviderClient

private const val TAG = "AppRoot"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppRoot(
    modifier: Modifier = Modifier,
    bottomClickHandler: BottomBarClickHandler,
    locationClient: FusedLocationProviderClient
) {
    val navController = rememberNavController()
    val permissionState = rememberPermissionState()
    val scaffoldState = rememberScaffoldState()
    (bottomClickHandler as Navigable).also { it.setNavController(navController) }

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(it) { data ->
                Snackbar(
                    actionColor = MaterialTheme.colors.primaryVariant,
                    snackbarData = data
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton( onClick = bottomClickHandler::onBottomRightClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_bluetooth_search_32),
                    contentDescription = stringResource(R.string.FABScanBluetoothDevicesDesc)
                )

            }
        },
        topBar = { TopAppBar( title = { AppTitle() }) },
        isFloatingActionButtonDocked = true,
        bottomBar = {
            var selected by remember { mutableStateOf(1) }
            BottomAppBar(cutoutShape = MaterialTheme.shapes.small.copy(
                CornerSize(percent = 50)
            )) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = {
                        bottomClickHandler.onBottomLeftClick();
                        selected = 0
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_devices_32),
                            contentDescription = stringResource(R.string.bottomNavSavedDevicesDesc),
                            tint = if (selected == 0) Color.White else Color(0xAAFFFFFF)
                        )
                    }
                    IconButton(onClick = {bottomClickHandler.onBottomCenterClick(); selected = 1}) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_map_24),
                            contentDescription = stringResource(R.string.bottomNavMapDesc),
                            tint = if (selected == 1) Color.White else Color(0xAAFFFFFF)
                        )
                    }
                    Spacer(modifier = Modifier.width(50.dp))
                }
            }
        }
    ) { padding ->
        Views(
            modifier = Modifier.padding(padding),
            navController = navController,
            permissions = permissionState,
            locationProviderClient = locationClient,
            scaffoldState = scaffoldState,
        )
    }
}
