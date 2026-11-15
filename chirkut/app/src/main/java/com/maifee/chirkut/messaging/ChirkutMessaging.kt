package com.maifee.chirkut.messaging

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.maifee.chirkut.data.MessagesRepository
import com.maifee.chirkut.service.MessagingService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Process-wide bridge between the Compose UI and {@link MessagingService}.
 *
 * - Starts + binds the service on first [ensureStarted]
 * - Exposes [send] / [connect] for the UI to drive outgoing wire traffic
 * - Listens for the service's incoming broadcasts, persists them to minidb,
 *   and republishes them through [incoming] for any observer to refresh
 */
@SuppressLint("StaticFieldLeak") // appContext only
object ChirkutMessaging {

    private const val TAG = "ChirkutMessaging"

    private val _incoming = MutableSharedFlow<IncomingMessage>(extraBufferCapacity = 64)
    val incoming: SharedFlow<IncomingMessage> = _incoming.asSharedFlow()

    private var appContext: Context? = null
    private var service: MessagingService? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as? MessagingService.LocalBinder)?.service()
            bound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            bound = false
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != MessagingService.ACTION_INCOMING) return
            val peer = intent.getStringExtra(MessagingService.EXTRA_PEER) ?: return
            val body = intent.getStringExtra(MessagingService.EXTRA_BODY) ?: return
            runCatching { MessagesRepository(context).insert(peer, body, outgoing = false) }
                .onFailure { Log.w(TAG, "persist incoming failed", it) }
            _incoming.tryEmit(IncomingMessage(peer, body))
        }
    }

    /** Idempotent: start + bind the service, register the incoming receiver. */
    fun ensureStarted(context: Context) {
        if (appContext != null) return
        val app = context.applicationContext
        appContext = app

        val intent = Intent(app, MessagingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            app.startForegroundService(intent)
        } else {
            app.startService(intent)
        }
        app.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        val filter = IntentFilter(MessagingService.ACTION_INCOMING)
        ContextCompat.registerReceiver(app, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    /** Opens an outbound socket to [peer] if we don't have one. Safe to call repeatedly. */
    fun connect(peer: String) {
        val svc = service ?: return
        runCatching { svc.connect(peer) }
            .onFailure { Log.w(TAG, "connect failed: $peer", it) }
    }

    /**
     * Sends [body] to [peer] through the service. Returns true on success;
     * on false the message was NOT put on the wire (service not bound, I/O
     * failure, …) and the caller should keep the draft or flag it.
     */
    fun send(peer: String, body: String): Boolean {
        val svc = service ?: return false
        return runCatching { svc.send(peer, body) }.isSuccess
    }

    data class IncomingMessage(val peer: String, val body: String)
}
