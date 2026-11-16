package com.maifee.chirkut.connect

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.Ndef
import android.util.Log
import kotlin.experimental.and

/**
 * Thin wrapper around {@link NfcAdapter#enableReaderMode}. Arm it with an
 * Activity + a payload handler; disarm from a DisposableEffect's onDispose.
 *
 * Supports two paths to pull the Chirkut URI off a nearby device:
 *  - Standard NDEF (Ndef tech) when a passive tag is read.
 *  - IsoDep (Type 4 / HCE) when the peer is another phone emitting via
 *    {@link ChirkutHceService}; we drive the same SELECT/READ APDUs here
 *    that our own HCE service answers there.
 *
 * @author Maifee Ul Asad
 */
object NfcReader {

    private const val TAG = "NfcReader"

    fun isSupported(activity: Activity): Boolean =
        NfcAdapter.getDefaultAdapter(activity) != null

    fun enable(activity: Activity, onPayload: (String) -> Unit) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        val flags = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
        adapter.enableReaderMode(activity, { tag -> handle(tag, onPayload) }, flags, null)
    }

    fun disable(activity: Activity) {
        NfcAdapter.getDefaultAdapter(activity)?.disableReaderMode(activity)
    }

    private fun handle(tag: Tag, onPayload: (String) -> Unit) {
        val fromNdef = readNdef(tag)
        if (fromNdef != null) { onPayload(fromNdef); return }
        val fromHce = readHce(tag)
        if (fromHce != null) onPayload(fromHce)
    }

    private fun readNdef(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val msg: NdefMessage? = ndef.ndefMessage ?: ndef.cachedNdefMessage
            msg?.records?.firstOrNull()?.let(::decodeUri)
        } catch (e: Exception) {
            Log.w(TAG, "Ndef read failed", e)
            null
        } finally {
            try { ndef.close() } catch (_: Exception) { }
        }
    }

    /** Phone-to-phone HCE: drive the Type 4 Tag flow against our own HCE service. */
    private fun readHce(tag: Tag): String? {
        val iso = IsoDep.get(tag) ?: return null
        return try {
            iso.connect()
            iso.timeout = 5_000
            if (!iso.transceive(hex("00A4040007D2760000850101")).endsOk()) return null
            if (!iso.transceive(hex("00A4000002E103")).endsOk()) return null
            val cc = iso.transceive(hex("00B000000F"))
            if (!cc.endsOk()) return null
            if (!iso.transceive(hex("00A4000002E104")).endsOk()) return null
            val lenResp = iso.transceive(hex("00B0000002"))
            if (!lenResp.endsOk() || lenResp.size < 4) return null
            val ndefLen = ((lenResp[0].toInt() and 0xFF) shl 8) or (lenResp[1].toInt() and 0xFF)
            if (ndefLen <= 0 || ndefLen > 4096) return null
            val out = ByteArray(ndefLen)
            var read = 0
            while (read < ndefLen) {
                val chunk = minOf(0xFA, ndefLen - read)
                val apdu = byteArrayOf(0x00, 0xB0.toByte(),
                    (((2 + read) ushr 8) and 0xFF).toByte(),
                    ((2 + read) and 0xFF).toByte(),
                    chunk.toByte())
                val resp = iso.transceive(apdu)
                if (!resp.endsOk() || resp.size < 2) return null
                val data = resp.copyOfRange(0, resp.size - 2)
                System.arraycopy(data, 0, out, read, data.size)
                read += data.size
            }
            decodeNdefUri(out)
        } catch (e: Exception) {
            Log.w(TAG, "HCE read failed", e)
            null
        } finally {
            try { iso.close() } catch (_: Exception) { }
        }
    }

    private fun decodeUri(record: android.nfc.NdefRecord): String? {
        val payload = record.payload ?: return null
        if (record.tnf == android.nfc.NdefRecord.TNF_WELL_KNOWN &&
            record.type.contentEquals(byteArrayOf('U'.code.toByte()))) {
            val prefix = URI_PREFIXES.getOrNull(payload[0].toInt() and 0xFF) ?: ""
            return prefix + String(payload, 1, payload.size - 1, Charsets.UTF_8)
        }
        return String(payload, Charsets.UTF_8)
    }

    private fun decodeNdefUri(ndef: ByteArray): String? {
        // Header byte ignored (we expect MB=1, SR=1, TNF=WKT, type=U); skip
        // to the payload length byte and read the U-record payload.
        if (ndef.size < 4) return null
        val payloadLen = ndef[2].toInt() and 0xFF
        val typeLen = ndef[1].toInt() and 0xFF
        val payloadStart = 3 + typeLen
        if (payloadStart + payloadLen > ndef.size) return null
        val prefix = URI_PREFIXES.getOrNull(ndef[payloadStart].toInt() and 0xFF) ?: ""
        return prefix + String(ndef, payloadStart + 1, payloadLen - 1, Charsets.UTF_8)
    }

    private fun ByteArray.endsOk(): Boolean =
        size >= 2 && this[size - 2] == 0x90.toByte() && (this[size - 1] and 0xFF.toByte()) == 0x00.toByte()

    private fun hex(s: String): ByteArray {
        val out = ByteArray(s.length / 2)
        for (i in out.indices) out[i] = s.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        return out
    }

    private val URI_PREFIXES = listOf(
        "", "http://www.", "https://www.", "http://", "https://", "tel:", "mailto:",
        "ftp://anonymous:anonymous@", "ftp://ftp.", "ftps://", "sftp://", "smb://",
        "nfs://", "ftp://", "dav://", "news:", "telnet://", "imap:", "rtsp://",
        "urn:", "pop:", "sip:", "sips:", "tftp:", "btspp://", "btl2cap://",
        "btgoep://", "tcpobex://", "irdaobex://", "file://", "urn:epc:id:",
        "urn:epc:tag:", "urn:epc:pat:", "urn:epc:raw:", "urn:epc:", "urn:nfc:",
    )
}
