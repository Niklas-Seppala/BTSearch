package com.asd.btsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import com.asd.btsearch.ui.events.BottomBarClickHandler
import com.asd.btsearch.ui.events.RootEvents
import com.asd.btsearch.ui.events.TopBarClickHandler
import com.asd.btsearch.ui.theme.BTSearchTheme
import com.asd.btsearch.ui.views.AppRoot

class MainActivity : ComponentActivity() {
    private val navEvents = RootEvents()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BTSearchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AppRoot(
                        topBarClickHandler = navEvents as TopBarClickHandler,
                        bottomClickHandler = navEvents as BottomBarClickHandler
                    )
                }
            }
        }
    }
}
