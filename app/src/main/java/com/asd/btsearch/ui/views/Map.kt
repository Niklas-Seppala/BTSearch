package com.asd.btsearch.ui.views

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.asd.btsearch.R
import com.asd.btsearch.repository.DeviceEntity
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


private const val TAG = "Map"
private const val MAP_ZOOM = 19.0

@Composable
fun composeMap(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
            setMultiTouchControls(true)
        }
    }
    return mapView
}

private const val spacing = 8
private const val height = 300
private val textPaint = Paint().also {
    it.style = Paint.Style.FILL
    it.color = Color.BLACK
    it.textSize = 30f
}
private val rectPaint = Paint().also {
    it.color = Color.WHITE
}


@Composable
fun Map(
    onDeviceClicked: (DeviceEntity) -> Unit,
    modifier: Modifier = Modifier,
    location: Location?,
    selectedDevice: DeviceEntity?,
    devices: List<DeviceEntity>
) {
    location ?: Log.d(TAG, "Location reading is null")
    selectedDevice ?: Log.d(TAG, "Device is null")

    Log.d(TAG, devices.toString())

    val map = composeMap()
    var mapInitialized by remember(map) { mutableStateOf(false) }
    val userLocationMarker by remember { mutableStateOf(Marker(map)) }

    if (!mapInitialized) {
        map.setTileSource(TileSourceFactory.MAPNIK)
        val context = LocalContext.current
        map.controller.setZoom(MAP_ZOOM)
        map.setMultiTouchControls(true)
        userLocationMarker.icon = context.getDrawable(R.drawable.ic_baseline_location_on_24)
        userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        userLocationMarker.closeInfoWindow()
        map.overlays.add(userLocationMarker)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mapInitialized = true
    }
    val context = LocalContext.current
    LaunchedEffect(devices) {
        map.overlays.forEach { map.overlays.remove(it) }
        map.overlays.add(userLocationMarker)
        devices.forEach { device ->
            val m = Marker(map)
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

            val bitmapResource = BitmapFactory.decodeResource(context.resources,
                R.drawable.bluetooth_marker
            )
            val dynamicTextWidth = textPaint.measureText(device.mac)
            val width = dynamicTextWidth.toInt()
            val bitmapWorkingCopy = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
            draw(bitmapWorkingCopy, bitmapResource, dynamicTextWidth, device.mac)
            m.icon = BitmapDrawable(context.resources, bitmapWorkingCopy)
            m.position = GeoPoint(device.lat, device.lon)

            m.setOnMarkerClickListener { g, y ->
                map.controller.animateTo(g.position)
                onDeviceClicked(device)
                true
            }
            map.overlays.add(m)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { map }) {
        if (selectedDevice != null) {
            Log.d(TAG, selectedDevice.mac)
            val device = GeoPoint(selectedDevice.lat, selectedDevice.lon)
            it.controller.setCenter(device)
            map.invalidate()
            return@AndroidView
        }

        location ?: return@AndroidView
        val currentPosition = GeoPoint(location.latitude, location.longitude)
        it.controller.setCenter(currentPosition)

        // Marker.
        userLocationMarker.position = currentPosition

        if (!mapInitialized) mapInitialized = true
        map.invalidate()
    }
}

private fun draw(
    bmWorkingCopy: Bitmap,
    bmIcon: Bitmap,
    dynamicTextWidth: Float,
    text: String
) {
    val canvas = Canvas(bmWorkingCopy)
    val iconX = 300 / 2 - bmIcon.width / 2
    val textX = 300 / 2 - dynamicTextWidth / 2
    val rect = Rect(iconX, 0, iconX + bmIcon.width, bmIcon.height)

    val ovalRect = RectF(
        textX,
        bmIcon.height.toFloat() + spacing,
        textX + dynamicTextWidth,
        bmIcon.height + 38f
    )
    canvas.drawRect(ovalRect, rectPaint)

    canvas.drawText(text, textX, bmIcon.height + 34f, textPaint)
    canvas.drawBitmap(bmIcon, null, rect, null)
}