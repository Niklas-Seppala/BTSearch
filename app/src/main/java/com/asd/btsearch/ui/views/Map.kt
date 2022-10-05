package com.asd.btsearch.ui.views

import android.location.Location
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.asd.btsearch.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private const val TAG = "Map"
private const val MAP_ZOOM = 18.0

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

@Composable
fun Map(modifier: Modifier = Modifier, location: Location?) {
    location ?: Log.d(TAG, "Location reading is null")

    val map = composeMap()
    var mapInitialized by remember(map) { mutableStateOf(false) }
    val marker by remember { mutableStateOf(Marker(map)) }

    if (!mapInitialized) {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(MAP_ZOOM)
        map.setMultiTouchControls(true)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.closeInfoWindow()
        map.overlays.add(marker)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mapInitialized = true
    }

    AndroidView(
        modifier = modifier,
        factory =  { map }) {
        location ?: return@AndroidView
        val currentPosition = GeoPoint(location.latitude, location.longitude)
        it.controller.setCenter(currentPosition)

        // Marker.
        marker.position = currentPosition

        if (!mapInitialized) mapInitialized = true
        map.invalidate()
    }
}