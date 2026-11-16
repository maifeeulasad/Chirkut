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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Process-wide bridge between the Compose UI and {@link MessagingService}.
 *
 * - Starts + binds the service on first [ensureStarted]
 * - Exposes [send] / [connect] for the UI to drive outgoing wire traffic
 * - Listens for the service's incoming broadcasts, persists them to minidb,
 *   and republishes them through [incoming] for any observer to refresh
 * @author Maifee Ul Asad
 */
@SuppressLint("StaticFieldLeak") // appContext only
object ChirkutMessaging {

    private const val TAG = "ChirkutMessaging"

    private val _incoming = MutableSharedFlow<IncomingMessage>(extraBufferCapacity = 64)
    val incoming: SharedFlow<IncomingMessage> = _incoming.asSharedFlow()

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

    /**
     * The host:port other devices should dial to reach this one. Null until
     * the service is bound. Safe to call from the UI thread.
     */
    fun listeningAddress(): String? = service?.listeningAddress()

    /**
     * Opens an outbound socket to [peer] if we don't have one. Idempotent.
     * Dispatched to IO because Client.<init> blocks on socket connect.
     */
    fun connect(peer: String) {
        val svc = service ?: return
        ioScope.launch {
            runCatching { svc.connect(peer) }
                .onFailure { Log.w(TAG, "connect failed: $peer", it) }
        }
    }

    /**
     * Sends [body] to [peer] through the service. Fire-and-forget; the UI
     * already has its own local bubble, so a wire failure is logged, not
     * surfaced. Runs on IO because Client.send blocks on the output stream.
     */
    fun send(peer: String, body: String) {
        val svc = service ?: return
        ioScope.launch {
            runCatching { svc.send(peer, body) }
                .onFailure { Log.w(TAG, "send failed: $peer <- $body", it) }
        }
    }

    data class IncomingMessage(val peer: String, val body: String)
}
