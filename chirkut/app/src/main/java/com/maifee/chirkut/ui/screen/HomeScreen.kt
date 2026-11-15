package com.maifee.chirkut.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.maifee.chirkut.data.MessagesRepository
import com.maifee.mingcute.MingcuteStyle
import com.maifee.mingcute.compose.MingcuteIcon
import com.maifee.velvet.component.button.ButtonShape
import com.maifee.velvet.component.button.ButtonType
import com.maifee.velvet.component.button.VelvetButton
import com.maifee.velvet.component.card.VelvetCard
import com.maifee.velvet.component.input.VelvetInput
import com.maifee.velvet.component.list.VelvetList
import com.maifee.velvet.reactive.rememberBinding

@Composable
fun HomeScreen(
    onOpenChat: (peer: String) -> Unit,
    onCall: (peer: String) -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { MessagesRepository(context) }
    var peers by remember { mutableStateOf(emptyList<String>()) }
    val newPeer = rememberBinding(defaultValue = "")

    LaunchedEffect(Unit) { peers = repo.recentPeers() }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        VelvetCard(title = "Chirkut", extra = {
            MingcuteIcon(
                name = "group",
                contentDescription = null,
                style = MingcuteStyle.LINE,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(24.dp),
            )
        }) {
            Text(
                "Peer-to-peer chat and calls. Nothing leaves this device.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        VelvetCard(title = "Connect to a peer") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VelvetInput(
                    modifier = Modifier.weight(1f),
                    value = newPeer,
                    placeholder = "192.168.49.2",
                    allowClear = true,
                )
                Spacer(Modifier.width(8.dp))
                VelvetButton(
                    onClick = {
                        val target = newPeer.value.trim()
                        if (target.isNotEmpty()) onOpenChat(target)
                    },
                    type = ButtonType.Primary,
                    icon = {
                        MingcuteIcon(
                            name = "chat_1",
                            contentDescription = null,
                            style = MingcuteStyle.LINE,
                        )
                    },
                ) { Text("Chat") }
                Spacer(Modifier.width(8.dp))
                VelvetButton(
                    onClick = {
                        val target = newPeer.value.trim()
                        if (target.isNotEmpty()) onCall(target)
                    },
                    type = ButtonType.Default,
                    shape = ButtonShape.Circle,
                    icon = {
                        MingcuteIcon(
                            name = "phone_call",
                            contentDescription = "Call",
                            style = MingcuteStyle.FILL,
                        )
                    },
                )
            }
        }

        VelvetCard(title = "Recent peers") {
            if (peers.isEmpty()) {
                Text(
                    "No conversations yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                VelvetList(items = peers, bordered = false) { peer ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MingcuteIcon(
                            name = "user_1",
                            contentDescription = null,
                            style = MingcuteStyle.LINE,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(peer, Modifier.weight(1f))
                        VelvetButton(
                            onClick = { onOpenChat(peer) },
                            type = ButtonType.Text,
                            icon = {
                                MingcuteIcon(
                                    name = "arrow_right",
                                    contentDescription = null,
                                    style = MingcuteStyle.LINE,
                                )
                            },
                        ) {}
                    }
                }
            }
        }
    }
}
