package com.maifee.chirkut.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.maifee.chirkut.call.CallDefaults
import com.maifee.chirkut.call.CallEndReason
import com.maifee.chirkut.call.CallListener
import com.maifee.chirkut.call.CallManager
import com.maifee.chirkut.call.CallRole
import com.maifee.chirkut.call.CallState
import com.maifee.chirkut.call.audio.AudioConfig
import com.maifee.chirkut.data.CallLogRepository
import com.maifee.mingcute.MingcuteStyle
import com.maifee.mingcute.compose.MingcuteIcon
import com.maifee.velvet.component.button.ButtonShape
import com.maifee.velvet.component.button.ButtonType
import com.maifee.velvet.component.button.VelvetButton

@Composable
fun CallScreen(
    peerAddress: String,
    incoming: Boolean,
    onEnded: () -> Unit,
) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(CallState.IDLE) }
    val callLog = remember { CallLogRepository(context) }
    val startedAt = remember { System.currentTimeMillis() }

    // Standalone CallManager for now. A follow-up will bind to CallService so
    // the mic foreground permission keeps the pipeline alive on API 31+.
    val manager = remember(peerAddress) {
        CallManager(
            AudioConfig.DEFAULT,
            CallDefaults.SIGNALING_PORT,
            CallDefaults.MEDIA_PORT,
            object : CallListener {
                override fun onStateChanged(next: CallState) { state = next }
                override fun onIncomingCall(remoteAddress: String) { }
                override fun onError(error: Throwable) { }
                override fun onCallEnded(reason: CallEndReason) {
                    val duration = System.currentTimeMillis() - startedAt
                    callLog.record(
                        peerAddress,
                        if (incoming) CallRole.CALLEE.name else CallRole.CALLER.name,
                        reason.name,
                        duration,
                    )
                    onEnded()
                }
            },
        )
    }

    DisposableEffect(peerAddress) {
        manager.start()
        if (!incoming) manager.dial(peerAddress)
        onDispose { manager.stop() }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MingcuteIcon(
            name = "user_1",
            contentDescription = null,
            style = MingcuteStyle.FILL,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier = Modifier.size(96.dp),
        )
        Spacer(Modifier.size(16.dp))
        Text(peerAddress, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.size(8.dp))
        Text(
            label(state, incoming),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.size(48.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (state == CallState.INCOMING_RINGING) {
                VelvetButton(
                    onClick = { manager.current()?.accept() },
                    type = ButtonType.Primary,
                    shape = ButtonShape.Circle,
                    icon = {
                        MingcuteIcon(
                            name = "phone_call",
                            contentDescription = "Accept",
                            style = MingcuteStyle.FILL,
                        )
                    },
                )
                Spacer(Modifier.width(24.dp))
            }
            VelvetButton(
                onClick = { manager.current()?.hangup() },
                type = ButtonType.Primary,
                shape = ButtonShape.Circle,
                danger = true,
                icon = {
                    MingcuteIcon(
                        name = "phone_off",
                        contentDescription = "Hang up",
                        style = MingcuteStyle.FILL,
                    )
                },
            )
        }
    }
}

private fun label(state: CallState, incoming: Boolean): String = when (state) {
    CallState.IDLE -> if (incoming) "Connecting…" else "Dialing…"
    CallState.OUTGOING_RINGING -> "Ringing…"
    CallState.INCOMING_RINGING -> "Incoming call"
    CallState.CONNECTING -> "Connecting…"
    CallState.ACTIVE -> "In call"
    CallState.ENDED -> "Call ended"
}
