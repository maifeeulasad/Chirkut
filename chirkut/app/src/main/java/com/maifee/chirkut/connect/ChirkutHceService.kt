package com.maifee.chirkut.connect

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

/**
 * Emits our current {@link ConnectInfo} as an NDEF Type-4 tag over NFC HCE.
 * The companion object holds the payload so the Compose UI can update it
 * without touching the service lifecycle.
 *
 * Protocol in brief (NFC Forum Type 4 Tag spec, slimmed to what any
 * phone-reader expects):
 *  - SELECT AID (D2760000850101) -> 90 00
 *  - SELECT CC file (E103) -> 90 00
 *  - READ BINARY CC -> 15-byte capability container
 *  - SELECT NDEF file (E104) -> 90 00
 *  - READ BINARY -> NDEF length (2 bytes) then message bytes
 *
 * @author Maifee Ul Asad
 */
class ChirkutHceService : HostApduService() {

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return SW_ERROR
        val payload = payload
        return when {
            matches(commandApdu, SELECT_AID) -> SW_OK
            matches(commandApdu, SELECT_CC) -> { fileSelected = FileKind.CC; SW_OK }
            matches(commandApdu, SELECT_NDEF) -> { fileSelected = FileKind.NDEF; SW_OK }
            commandApdu.size >= 5 && commandApdu[1] == INS_READ_BINARY -> handleRead(commandApdu, payload)
            else -> SW_ERROR
        }
    }

    override fun onDeactivated(reason: Int) {
        fileSelected = FileKind.NONE
        Log.d(TAG, "HCE deactivated: $reason")
    }

    private var fileSelected = FileKind.NONE

    private fun handleRead(apdu: ByteArray, payload: String): ByteArray {
        val offset = ((apdu[2].toInt() and 0xFF) shl 8) or (apdu[3].toInt() and 0xFF)
        val len = (apdu[4].toInt() and 0xFF).let { if (it == 0) 256 else it }
        val source = when (fileSelected) {
            FileKind.CC -> CAPABILITY_CONTAINER
            FileKind.NDEF -> ndefBytes(payload)
            FileKind.NONE -> return SW_ERROR
        }
        val from = offset.coerceAtMost(source.size)
        val to = (offset + len).coerceAtMost(source.size)
        val slice = source.copyOfRange(from, to)
        return slice + SW_OK
    }

    private enum class FileKind { NONE, CC, NDEF }

    companion object {
        private const val TAG = "ChirkutHceService"

        /** Payload exposed by the service; writers set this from the UI. */
        @Volatile
        var payload: String = ""

        private val SW_OK = byteArrayOf(0x90.toByte(), 0x00)
        private val SW_ERROR = byteArrayOf(0x6A.toByte(), 0x82.toByte())

        private const val INS_READ_BINARY: Byte = 0xB0.toByte()

        private val SELECT_AID = hex("00A4040007D2760000850101")
        private val SELECT_CC = hex("00A4000002E103")
        private val SELECT_NDEF = hex("00A4000002E104")

        // 15-byte CC per NFC Forum Type 4 Tag Technical Spec v3.
        private val CAPABILITY_CONTAINER = hex(
            "000F2000FFFFFFFF0406E10400FF00FF",
        ).copyOfRange(0, 15)

        private fun ndefBytes(text: String): ByteArray {
            val bytes = text.toByteArray(Charsets.UTF_8)
            // NDEF URI record, no prefix abbreviation (0x00), payload = text.
            val payload = byteArrayOf(0x00) + bytes
            val header = byteArrayOf(
                0xD1.toByte(),         // MB=1 ME=1 CF=0 SR=1 IL=0 TNF=001 (WKT)
                0x01,                   // type length = 1
                payload.size.toByte(),  // payload length (short record)
                'U'.code.toByte(),      // type = "U" (URI record)
            )
            val ndef = header + payload
            val nlen = byteArrayOf(((ndef.size ushr 8) and 0xFF).toByte(), (ndef.size and 0xFF).toByte())
            return nlen + ndef
        }

        private fun matches(apdu: ByteArray, select: ByteArray): Boolean {
            if (apdu.size < select.size) return false
            for (i in select.indices) if (apdu[i] != select[i]) return false
            return true
        }

        private fun hex(s: String): ByteArray {
            val out = ByteArray(s.length / 2)
            for (i in out.indices) out[i] = s.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            return out
        }
    }
}
