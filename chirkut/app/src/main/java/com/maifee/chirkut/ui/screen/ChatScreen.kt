package com.maifee.chirkut.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.maifee.chirkut.data.ChirkutMessage
import com.maifee.chirkut.data.MessagesRepository
import com.maifee.chirkut.messaging.ChirkutMessaging
import com.maifee.mingcute.MingcuteStyle
import com.maifee.mingcute.compose.MingcuteIcon
import com.maifee.velvet.component.button.ButtonShape
import com.maifee.velvet.component.button.ButtonType
import com.maifee.velvet.component.button.VelvetButton
import com.maifee.velvet.component.input.VelvetInput
import com.maifee.velvet.reactive.rememberBinding

@Composable
fun ChatScreen(
    peerAddress: String,
    onBack: () -> Unit,
    onCall: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember(peerAddress) { MessagesRepository(context) }
    var messages by remember { mutableStateOf(emptyList<ChirkutMessage>()) }
    val draft = rememberBinding(defaultValue = "")
    val listState = rememberLazyListState()

    fun refresh() { messages = repo.conversation(peerAddress) }

    LaunchedEffect(peerAddress) {
        ChirkutMessaging.connect(peerAddress)
        refresh()
    }

    LaunchedEffect(peerAddress) {
        ChirkutMessaging.incoming.collect { arrived ->
            if (arrived.peer == peerAddress) refresh()
        }
    }

    Column(Modifier.fillMaxSize()) {
        ChatHeader(peer = peerAddress, onBack = onBack, onCall = onCall)
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(messages, key = { it.id ?: it.createdAt.toString() }) { msg ->
                MessageBubble(msg)
            }
        }

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
        }

        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VelvetInput(
                modifier = Modifier.weight(1f),
                value = draft,
                placeholder = "Message",
                allowClear = true,
            )
            Spacer(Modifier.width(8.dp))
            VelvetButton(
                onClick = {
                    val body = draft.value.trim()
                    if (body.isNotEmpty()) {
                        repo.insert(peer = peerAddress, body = body, outgoing = true)
                        ChirkutMessaging.send(peerAddress, body)
                        draft.value = ""
                        refresh()
                    }
                },
                type = ButtonType.Primary,
                shape = ButtonShape.Circle,
                icon = {
                    MingcuteIcon(
                        name = "send_plane",
                        contentDescription = "Send",
                        style = MingcuteStyle.FILL,
                    )
                },
            )
        }
    }
}

@Composable
private fun ChatHeader(peer: String, onBack: () -> Unit, onCall: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VelvetButton(
            onClick = onBack,
            type = ButtonType.Text,
            icon = {
                MingcuteIcon(
                    name = "arrow_left",
                    contentDescription = "Back",
                    style = MingcuteStyle.LINE,
                )
            },
        ) {}
        Spacer(Modifier.width(8.dp))
        Text(
            peer,
            Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
        )
        VelvetButton(
            onClick = onCall,
            type = ButtonType.Primary,
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

@Composable
private fun MessageBubble(message: ChirkutMessage) {
    val cs = MaterialTheme.colorScheme
    val outgoing = message.outgoing
    val bubble = if (outgoing) cs.primary else cs.surfaceVariant
    val onBubble = if (outgoing) cs.onPrimary else cs.onSurface
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (outgoing) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bubble)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                message.body,
                color = onBubble,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
