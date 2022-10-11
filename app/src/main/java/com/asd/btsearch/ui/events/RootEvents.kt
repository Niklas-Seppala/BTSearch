package com.asd.btsearch.ui.events

import android.util.Log
import androidx.navigation.NavHostController
import com.asd.btsearch.ui.navigation.Navigable
import com.asd.btsearch.ui.navigation.Screen

private const val TAG = "RootEvents"

class RootEvents : TopBarClickHandler, BottomBarClickHandler, Navigable {
    private lateinit var navController: NavHostController

    override fun onBottomLeftClick() {
        navigateTo(Screen.Stats.baseRoute)
    }

    override fun onBottomCenterClick() {
        navigateTo(Screen.Home.withArgs("-1"))
    }

    override fun onBottomRightClick() {
        navigateTo(Screen.Info.baseRoute)
    }

    override fun onSettingsClick() {
        navigateTo(Screen.Settings.baseRoute)
    }

    override fun setNavController(navController: NavHostController) {
        this.navController = navController
        Log.d(TAG, "RootEvents::navController set to " +
                "${System.identityHashCode(navController)}")
    }

    private fun navigateTo(dest: String): Boolean {
        val current = navController.currentDestination?.route
        if (dest != current) {
            Log.d(TAG, "Navigating to $dest")
            navController.navigate(dest)
            return true
        }
        Log.d(TAG, "Already at $dest")
        return false;
    }
}