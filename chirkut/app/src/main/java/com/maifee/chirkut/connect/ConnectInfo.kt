package com.maifee.chirkut.connect

/**
 * Share payload for "here's how to reach me". Encoded as
 * {@code chirkut://<host>:<port>} so a scanner can distinguish a Chirkut
 * invite from an arbitrary URL. Decoding is tolerant: a bare
 * {@code host:port} also parses, which keeps the payload readable by a
 * human who just copy-pastes it.
 * @author Maifee Ul Asad
 */
data class ConnectInfo(val host: String, val port: Int) {

    /** Dialable as-is by [com.maifee.chirkut.network.Client]. */
    val peerId: String get() = "$host:$port"

    /** Compact text form for QR/NFC/clipboard. */
    fun encode(): String = "$SCHEME://$host:$port"

    companion object {
        const val SCHEME = "chirkut"

        private val URI_RE = Regex("""^(?:chirkut://)?([^:/\s]+):(\d+)$""")

        /** Parses [raw] back into a [ConnectInfo], or null when it doesn't look like one. */
        fun parse(raw: String): ConnectInfo? {
            val m = URI_RE.matchEntire(raw.trim()) ?: return null
            val port = m.groupValues[2].toIntOrNull() ?: return null
            if (port !in 1..65_535) return null
            return ConnectInfo(m.groupValues[1], port)
        }
    }
}
