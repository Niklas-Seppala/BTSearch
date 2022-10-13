package com.asd.btsearch


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import com.asd.btsearch.ui.events.BottomBarClickHandler
import com.asd.btsearch.ui.events.RootEvents
import com.asd.btsearch.ui.theme.BTSearchTheme
import com.asd.btsearch.ui.views.AppRoot
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration


class MainActivity : ComponentActivity() {
    private val navEvents = RootEvents()
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSystemService(Context.LAYOUT_INFLATER_SERVICE)

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            BTSearchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AppRoot(
                        bottomClickHandler = navEvents as BottomBarClickHandler,
                        locationClient =  fusedLocationClient
                    )
                }
            }
        }
    }
}
