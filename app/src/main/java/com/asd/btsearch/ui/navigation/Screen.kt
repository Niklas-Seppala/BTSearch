package com.asd.btsearch.ui.navigation

internal sealed class Screen(private val route: String) {
    object Home : Screen("home")
    object Stats : Screen("stats")
    object Tracing : Screen("tracing")
    object Settings : Screen("settings")

    val baseRoute = route

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { append("/$it") }
        }
    }
}