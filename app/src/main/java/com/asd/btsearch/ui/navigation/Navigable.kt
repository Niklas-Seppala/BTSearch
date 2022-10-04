package com.asd.btsearch.ui.navigation

import androidx.navigation.NavHostController

/**
 * Makes it possible to inject application's NavHostController
 * into this object, so it can be utilized in inner logic
 * of this object.
 */
interface Navigable {
    /**
     * Inject navigation controller into this object.
     *
     * @param navController Application's navigation controller
     */
    fun setNavController(navController: NavHostController)
}