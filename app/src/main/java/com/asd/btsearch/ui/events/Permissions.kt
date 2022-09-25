package com.asd.btsearch.ui.events

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

private const val TAG = "PERMISSIONS"

/**
 *
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberPermissionState() : MultiplePermissionsState? {
    if (LocalContext.current.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        Log.d(TAG,"Device supports BLE.")
    } else {
        Log.e(TAG, "Device does not support BLE.")
        return null
    }

    val permissionState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            rememberMultiplePermissionsState(
                permissions = listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            Log.d(TAG, "VERSION.SDK_INT < 31")
            rememberMultiplePermissionsState(
                permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d(TAG, "Requesting permissions: " +
                        permissionState.permissions.joinToString {"${it.permission} "}
                )
                permissionState.launchMultiplePermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    })

    return permissionState
}