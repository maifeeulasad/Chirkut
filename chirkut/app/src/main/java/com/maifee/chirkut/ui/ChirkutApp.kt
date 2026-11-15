package com.maifee.chirkut.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.maifee.chirkut.ui.screen.CallScreen
import com.maifee.chirkut.ui.screen.ChatScreen
import com.maifee.chirkut.ui.screen.HomeScreen
import com.maifee.velvet.VelvetConfig
import com.maifee.velvet.component.app.VelvetApp

/** Minimal in-memory navigation; three destinations is not worth a graph. */
internal sealed class Route {
    data object Home : Route()
    data class Chat(val peerAddress: String) : Route()
    data class Call(val peerAddress: String, val incoming: Boolean) : Route()
}

@Composable
fun ChirkutApp() {
    VelvetApp(config = VelvetConfig()) {
        var route by remember { mutableStateOf<Route>(Route.Home) }
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            when (val r = route) {
                Route.Home -> HomeScreen(
                    onOpenChat = { route = Route.Chat(it) },
                    onCall = { route = Route.Call(it, incoming = false) },
                )
                is Route.Chat -> ChatScreen(
                    peerAddress = r.peerAddress,
                    onBack = { route = Route.Home },
                    onCall = { route = Route.Call(r.peerAddress, incoming = false) },
                )
                is Route.Call -> CallScreen(
                    peerAddress = r.peerAddress,
                    incoming = r.incoming,
                    onEnded = { route = Route.Home },
                )
            }
        }
    }
}

private fun Modifier.background(color: Color): Modifier =
    this.then(androidx.compose.foundation.background(color))
