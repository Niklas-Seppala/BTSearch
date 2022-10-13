package com.asd.btsearch.ui.views

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.FileProvider
import com.asd.btsearch.BuildConfig
import com.asd.btsearch.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "CameraView"
private const val AUTH = "${BuildConfig.APPLICATION_ID}.fileprovider"

fun pictureFilePath(context: Context, deviceId: Int): Pair<Uri, String> {
    val dirPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File.createTempFile("$deviceId-img", ".jpg", dirPath)
    return Pair(
        FileProvider.getUriForFile(context, AUTH, imageFile),
        imageFile.absolutePath
    )
}

@Composable
fun CameraView(
    deviceId: Int,
    onSuccess: (suspend () -> Unit)? = null,
    onCancel: (suspend () -> Unit)? = null
) {
    val context = LocalContext.current
    val (uri, path) = pictureFilePath(context, deviceId)
    var result by remember { mutableStateOf<Bitmap?>(null) }
    val scope = rememberCoroutineScope()
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        scope.launch(Dispatchers.IO) {
            if (it) {
                result = BitmapFactory.decodeFile(path)
                if (result == null) {
                    Log.e(TAG, "Could not save null bitmap")
                    onCancel?.invoke()
                    return@launch
                }
                context.openFileOutput("$deviceId.jpg", MODE_PRIVATE).use {
                    result?.compress(Bitmap.CompressFormat.JPEG, 80, it)
                }
                if (result == null)
                Log.d(TAG, "Pic for device $deviceId saved.")

                onSuccess?.invoke()
            } else {
                Log.d(TAG, "Pic for device $deviceId cancelled, was not saved.")
                onCancel?.invoke()
            }
        }

    }

    Button(onClick = { cameraLauncher.launch(uri) }) {
        Icon(painter = painterResource(id = R.drawable.ic_baseline_camera_alt_24), "")
    }
}