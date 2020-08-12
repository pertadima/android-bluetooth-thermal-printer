package id.co.printer.utility

import android.graphics.Bitmap
import android.graphics.Matrix
import id.co.printer.enum.EnumTextAlignment
import id.co.printer.enum.EnumTextSize
import java.io.IOException
import java.io.OutputStream

/**
 * Created by pertadima on 12,August,2020
 */

fun OutputStream?.printText(msg: ByteArray) {
    try {
        // Print normal text
        this?.write(msg)
        printNewLine()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun OutputStream?.printNewLine() {
    try {
        this?.write(PrinterCommands.FEED_LINE)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun OutputStream?.printCustom(msg: String, size: EnumTextSize, align: EnumTextAlignment) {
    //Print config "mode"
    val cc = byteArrayOf(0x1B, 0x21, 0x03) // 0- normal size text
    val bb = byteArrayOf(0x1B, 0x21, 0x08) // 1- only bold text
    val bb2 = byteArrayOf(0x1B, 0x21, 0x20) // 2- bold with medium text
    val bb3 = byteArrayOf(0x1B, 0x21, 0x10) // 3- bold with large text

    try {
        when (size) {
            EnumTextSize.NORMAL -> this?.write(cc)
            EnumTextSize.BOLD -> this?.write(bb)
            EnumTextSize.MEDIUM_BOLD -> this?.write(bb2)
            EnumTextSize.LARGE_BOLD -> this?.write(bb3)
        }
        when (align) {
            EnumTextAlignment.LEFT -> this?.write(PrinterCommands.ESC_ALIGN_LEFT)
            EnumTextAlignment.CENTER -> this?.write(PrinterCommands.ESC_ALIGN_CENTER)
            EnumTextAlignment.RIGHT -> this?.write(PrinterCommands.ESC_ALIGN_RIGHT)
        }
        this?.write(msg.toByteArray())
        this?.write(cc);
        printNewLine();
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun OutputStream?.printPhoto(img: Bitmap) {
    try {
        val newBitmap: Bitmap? = if (img.width > 200 || img.height > 200) {
            getResizedBitmap(img, 200, 200)
        } else {
            img
        }

        newBitmap?.let {
            val command: ByteArray? = PrinterUtils.decodeBitmap(it)
            this?.write(PrinterCommands.ESC_ALIGN_CENTER)
            command?.let { byteArray ->
                printText(byteArray)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
    val width = bm.width
    val height = bm.height
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    // CREATE A MATRIX FOR THE MANIPULATION
    val matrix = Matrix()
    // RESIZE THE BIT MAP
    matrix.postScale(scaleWidth, scaleHeight)

    // "RECREATE" THE NEW BITMAP
    val resizedBitmap = Bitmap.createBitmap(
        bm, 0, 0, width, height, matrix, false
    )
    bm.recycle()
    return resizedBitmap
}
