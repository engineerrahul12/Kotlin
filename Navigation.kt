package com.rahulsah.studio.ui

sealed class Screen(val route: String) {
    object Home       : Screen("home")
    object Downloader : Screen("downloader")
    object Browser    : Screen("browser/{url}") {
        fun createRoute(url: String) = "browser/${java.net.URLEncoder.encode(url, "UTF-8")}"
    }
    object Library    : Screen("library")
    object Editor     : Screen("editor?uri={uri}&type={type}") {
        fun createRoute(uri: String, type: String = "VIDEO") =
            "editor?uri=${java.net.URLEncoder.encode(uri, "UTF-8")}&type=$type"
    }
    object Queue      : Screen("queue")
    object Settings   : Screen("settings")
}
