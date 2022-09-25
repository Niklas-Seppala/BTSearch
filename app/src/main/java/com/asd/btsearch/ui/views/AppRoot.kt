package com.asd.btsearch.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.asd.btsearch.R
import com.asd.btsearch.ui.events.BottomBarClickHandler
import com.asd.btsearch.ui.events.TopBarClickHandler
import com.asd.btsearch.ui.events.rememberPermissionState
import com.asd.btsearch.ui.navigation.Navigable
import com.asd.btsearch.ui.navigation.Views
import com.google.accompanist.permissions.ExperimentalPermissionsApi

private const val TAG = "AppRoot"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppRoot(
    modifier: Modifier = Modifier,
    topBarClickHandler: TopBarClickHandler,
    bottomClickHandler: BottomBarClickHandler,
) {
    val navController = rememberNavController()
    val permissionState = rememberPermissionState()
    (topBarClickHandler as Navigable).also { it.setNavController(navController) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { AppTitle() },
                actions = {
                    IconButton(onClick = topBarClickHandler::onSettingsClick) {
                        Icon(Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.topBarSettingsIconDesc))
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = bottomClickHandler::onBottomLeftClick) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Localized description")
                    }
                    IconButton(onClick = bottomClickHandler::onBottomCenterClick ) {
                        Icon(Icons.Filled.Home, contentDescription = "Localized description")
                    }
                    IconButton(onClick = bottomClickHandler::onBottomRightClick) {
                        Icon(Icons.Filled.Info, contentDescription = "Localized description")
                    }
                }
            }
        }
    ) {
        Views(navController = navController, permissions = permissionState)
    }
}
