package com.maifee.chirkut.connect

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Encodes [payload] as a real, scannable QR bitmap at [sizePx] × [sizePx].
 * Used until velvet's VelvetQRCode becomes spec-compliant — swap this
 * helper for `VelvetQRCode(value = payload)` at that point.
 *
 * Runs on the caller's thread; feed it from a background dispatcher if
 * the payload is long or the size is large.
 *
 * @author Maifee Ul Asad
 */
object QrCode {

    fun encode(payload: String, sizePx: Int = 512): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1,
        )
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bmp = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
