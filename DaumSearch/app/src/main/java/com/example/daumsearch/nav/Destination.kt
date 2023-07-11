package com.example.daumsearch.nav

interface Destination {
    val route: String
}

object MainRoute : Destination {
    override val route = "main"
}

object WebViewRoute : Destination {
    override val route = "webview"
}