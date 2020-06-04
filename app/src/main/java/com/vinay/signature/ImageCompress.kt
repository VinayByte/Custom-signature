package com.vinay.signature

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.bumptech.glide.gifdecoder.GifHeaderParser
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.sqrt

object ImageCompress {

    fun compressImageDataWithWidth(rawData: ByteArray, width: Int): ByteArray? {
//        if (rawData.size <= 30000) {
//            return rawData
//        }
        val format = rawData.imageFormat()
        if (format == ImageFormat.UNKNOWN) {
            return null
        }

        val (imageWidth, imageHeight) = rawData.imageSize()
        val longSideWidth = max(imageWidth, imageHeight)

        if (longSideWidth <= width) {
            return rawData
        }

        var resultData: ByteArray? = null
        val image = BitmapFactory.decodeByteArray(rawData, 0, rawData.size)
        val ratio = width.toDouble() / longSideWidth.toDouble()
        val resizeImageFrame = Bitmap.createScaledBitmap(
            image,
            (image.width.toDouble() * ratio).toInt(),
            (image.height.toDouble() * ratio).toInt(),
            true
        )
        image.recycle()
        when (format) {
            ImageFormat.PNG -> {
                resultData = resizeImageFrame.toByteArray(Bitmap.CompressFormat.PNG)
            }
            ImageFormat.JPG -> {
                resultData = resizeImageFrame.toByteArray(Bitmap.CompressFormat.JPEG)
            }
            else -> {
            }
        }
        resizeImageFrame.recycle()
        return resultData
    }


    fun compressImageWithSize(rawData: ByteArray, limitDataSize: Int): ByteArray? {
        if (rawData.size <= limitDataSize) {
            return rawData
        }

        val format = rawData.imageFormat()
        if (format == ImageFormat.UNKNOWN) {
            return null
        }

        var resultData = rawData

        if (format == ImageFormat.JPG) {
            var compression = 100
            var maxCompression = 100
            var minCompression = 0

            try {
                val outputStream = ByteArrayOutputStream()
                for (index in 0..6) {
                    compression = (maxCompression + minCompression) / 2
                    Log.d("@", "compression-$compression")
                    outputStream.reset()
                    val image = BitmapFactory.decodeByteArray(rawData, 0, rawData.size)
                    image.compress(Bitmap.CompressFormat.JPEG, compression, outputStream)
                    image.recycle()
                    resultData = outputStream.toByteArray()
                    if (resultData.size < (limitDataSize.toDouble() * 0.9).toInt()) {
                        minCompression = compression
                    } else if (resultData.size > limitDataSize) {
                        maxCompression = compression
                    } else {
                        break
                    }
                }
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (resultData.size <= limitDataSize) {
                return resultData
            }
        }

        val (imageWidth, imageHeight) = resultData.imageSize()
        var longSideWidth = max(imageWidth, imageHeight)

        while (resultData.size > limitDataSize) {
            val ratio = sqrt(limitDataSize.toDouble() / resultData.size.toDouble())
            longSideWidth = (longSideWidth.toDouble() * ratio).toInt()
            val data = compressImageDataWithWidth(resultData, longSideWidth)
            if (data != null) {
                resultData = data
            } else {
                return null
            }
        }
        return resultData
    }


}

fun ByteArray.imageFormat(): ImageFormat {
    val headerData = this.slice(0..2)
    val hexString =
        headerData.fold(StringBuilder("")) { result, byte ->
            result.append(
                (byte.toInt() and 0xFF).toString(
                    16
                )
            )
        }
            .toString().toUpperCase()
    var imageFormat = ImageFormat.UNKNOWN
    when (hexString) {
        "FFD8FF" -> {
            imageFormat = ImageFormat.JPG
        }
        "89504E" -> {
            imageFormat = ImageFormat.PNG
        }
    }
    return imageFormat
}


fun Bitmap.toByteArray(format: Bitmap.CompressFormat): ByteArray? {
    var data: ByteArray? = null
    try {
        val outputStream = ByteArrayOutputStream()
        this.compress(format, 100, outputStream)
        data = outputStream.toByteArray()
        outputStream.close()
        return data
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return data
}

private fun ByteArray.imageSize(): Pair<Int, Int> {
    var imageWidth = 0
    var imageHeight = 0

    try {
        val imageFrame = BitmapFactory.decodeByteArray(this, 0, this.size)
        imageWidth = imageFrame.width
        imageHeight = imageFrame.height
        imageFrame.recycle()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
    return Pair(imageWidth, imageHeight)
}

enum class ImageFormat {
    JPG, PNG, UNKNOWN
}


