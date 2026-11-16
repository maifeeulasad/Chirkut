package com.maifee.chirkut.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.maifee.chirkut.connect.ChirkutHceService
import com.maifee.chirkut.connect.ConnectInfo
import com.maifee.chirkut.connect.NfcReader
import com.maifee.chirkut.connect.QrCode
import com.maifee.chirkut.messaging.ChirkutMessaging
import com.maifee.mingcute.MingcuteStyle
import com.maifee.mingcute.compose.MingcuteIcon
import com.maifee.velvet.component.button.ButtonType
import com.maifee.velvet.component.button.VelvetButton
import com.maifee.velvet.component.card.VelvetCard
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Share-my-address screen. Three fallbacks on one page so the user picks
 * whichever works where they are:
 *
 * 1. **QR code** — hands the full `chirkut://host:port` payload to a
 *    scanner. Encoded with ZXing (velvet's qrcode is a placeholder today).
 * 2. **Plain text + copy** — the same string, large enough to read aloud
 *    or type by hand, plus one-tap copy.
 * 3. **NFC / scan** — buttons to jump to the scanner flow or to arm NFC
 *    (NFC emitter arrives in a follow-up).
 *
 * @author Maifee Ul Asad
 */
@Composable
fun ShareScreen(
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
) {
    val context = LocalContext.current
    var address by remember { mutableStateOf<String?>(null) }
    var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var scanError by remember { mutableStateOf<String?>(null) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val raw = result?.contents ?: return@rememberLauncherForActivityResult
        val info = ConnectInfo.parse(raw)
        if (info != null) {
            onOpenChat(info.peerId)
        } else {
            scanError = "Scanned \"$raw\" doesn't look like a Chirkut address."
        }
    }

    fun launchScan() {
        scanError = null
        scanLauncher.launch(
            ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Scan a Chirkut QR")
                .setBeepEnabled(false)
                .setOrientationLocked(false),
        )
    }

    LaunchedEffect(Unit) {
        // The service bind can lag the first composition slightly.
        var attempts = 0
        while (attempts < 20 && address == null) {
            address = ChirkutMessaging.listeningAddress()
            if (address == null) { kotlinx.coroutines.delay(100); attempts++ }
        }
        val addr = address ?: return@LaunchedEffect
        val info = ConnectInfo.parse(addr) ?: return@LaunchedEffect
        ChirkutHceService.payload = info.encode()
        qrBitmap = withContext(Dispatchers.Default) { QrCode.encode(info.encode()) }
    }

    // While this screen is up, read any NFC tag or phone presented. The
    // emitter-side HCE service keeps answering independently of this effect.
    val activity = findActivity(context)
    DisposableEffect(activity) {
        if (activity != null) {
            NfcReader.enable(activity) { raw ->
                ConnectInfo.parse(raw)?.let(onOpenChat)
            }
        }
        onDispose { if (activity != null) NfcReader.disable(activity) }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
            Text(
                "Share your Chirkut",
                Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.titleLarge,
            )
        }

        VelvetCard(title = "Scan this QR") {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val bmp = qrBitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Chirkut QR code",
                        modifier = Modifier.size(240.dp),
                    )
                } else {
                    Text(
                        "Preparing…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        VelvetCard(title = "Or share as text") {
            Column {
                Text(
                    address ?: "Waiting for the messaging service…",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                )
                Spacer(Modifier.size(12.dp))
                Row {
                    VelvetButton(
                        onClick = {
                            val value = address ?: return@VelvetButton
                            copyToClipboard(context, value)
                        },
                        type = ButtonType.Default,
                        icon = {
                            MingcuteIcon(
                                name = "copy_2",
                                contentDescription = null,
                                style = MingcuteStyle.LINE,
                            )
                        },
                    ) { Text("Copy") }
                    Spacer(Modifier.width(12.dp))
                    VelvetButton(
                        onClick = { launchScan() },
                        type = ButtonType.Primary,
                        icon = {
                            MingcuteIcon(
                                name = "scan",
                                contentDescription = null,
                                style = MingcuteStyle.LINE,
                            )
                        },
                    ) { Text("Scan theirs") }
                }
                val err = scanError
                if (err != null) {
                    Spacer(Modifier.size(8.dp))
                    Text(err, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        VelvetCard(title = "NFC") {
            Text(
                "Hold the back of your phones together. Whoever opened this " +
                    "screen will emit the address; the other side's reader " +
                    "picks it up and opens the chat automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Chirkut address", text))
}

private fun findActivity(context: Context): android.app.Activity? {
    var c: Context? = context
    while (c is android.content.ContextWrapper) {
        if (c is android.app.Activity) return c
        c = c.baseContext
    }
    return null
}
