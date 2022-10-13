package com.asd.btsearch.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.asd.btsearch.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoViewModel : ViewModel() {
    val currentImage: MutableLiveData<Bitmap?> = MutableLiveData()

    suspend fun loadImage(ctx: Context, deviceId: Int) {
        withContext(Dispatchers.IO) {
            runCatching {
                val istream = ctx.openFileInput("$deviceId.jpg")
                val bm = BitmapFactory.decodeStream(istream)
                istream.close()
                currentImage.postValue(bm);
            }
        }
    }
}

@Composable
fun PhotoView(
    modifier: Modifier = Modifier,
    deviceId: Int,
    mac: String,
    photoViewModel: PhotoViewModel = PhotoViewModel()
) {
    val ctx = LocalContext.current
    val photo by photoViewModel.currentImage.observeAsState()

    LaunchedEffect(key1 = deviceId) {
        photoViewModel.loadImage(ctx, deviceId)
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (photo == null) {
            CircularProgressIndicator(modifier = Modifier.size(120.dp))
        } else {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(90f),
                bitmap = photo!!.asImageBitmap(),
                contentDescription = stringResource(R.string.photoViewDesc)
            )
        }
    }
}















