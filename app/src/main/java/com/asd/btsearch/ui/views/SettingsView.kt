package com.asd.btsearch.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

private const val TAG = "SettingsView"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsView(navigation: NavHostController, permissionsState: MultiplePermissionsState) {
    Box(Modifier.fillMaxSize()) {
        Text(text = TAG, modifier = Modifier.align(Alignment.Center))
    }
}