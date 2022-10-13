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

object Permissions {
    @OptIn(ExperimentalPermissionsApi::class)
    fun hasLocation(state: MultiplePermissionsState?): Boolean {
        state?.permissions?.forEach {
            when (it.permission) {
                Manifest.permission.ACCESS_FINE_LOCATION -> {
                    return it.hasPermission
                }
            }
        }
        Log.d(TAG, "Missing ACCESS_FINE_LOCATION")
        return false
    }
}

/**
 * Composable wrapper function for making sure user has
 * provided required permissions for this application
 * to function. Registers request callback on lifecycle's
 * onResume() method.
 *
 * if returned value is null, then running device does
 * not support Bluetooth LE.
 *
 * @return nullable MultiplePermissionsState object.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberPermissionState(): MultiplePermissionsState? {
    if (LocalContext.current.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        Log.d(TAG, "Device supports BTLE.")
    } else {
        Log.e(TAG, "Device does not support BTLE.")
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
                        permissionState.permissions.joinToString { "${it.permission} " }
                )
                permissionState.launchMultiplePermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    })

    return permissionState
}