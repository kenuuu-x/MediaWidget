package com.begonia.mediawidget.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import java.io.File
import java.io.FileOutputStream

class ImagePipeline(private val context: Context) {
    private var sourceId: Int? = null
    private var sourceBitmap: Bitmap? = null

    private val transformations = mutableListOf<(Bitmap) -> Bitmap>()

    fun fromRes(resId: Int): ImagePipeline {
        this.sourceId = resId
        return this
    }

    fun fromBitmap(bitmap: Bitmap): ImagePipeline {
        this.sourceBitmap = bitmap
        return this
    }

    fun scale(
        width: Int = WIDGET_IMAGE_SIZE_PX,
        height: Int = WIDGET_IMAGE_SIZE_PX
    ): ImagePipeline {
        transformations.add { bitmap ->
            if (bitmap.width != width || bitmap.height != height) {
                bitmap.scale(width, height)
            } else {
                bitmap
            }
        }
        return this
    }

    fun roundCorners(radius: Float = 52f): ImagePipeline {
        transformations.add { bitmap -> bitmap.createRoundedBitmap(radius) }
        return this
    }

    fun process(): Bitmap {
        var bitmap = sourceBitmap
            ?: sourceId?.let { BitmapFactory.decodeResource(context.resources, it) }
            ?: error("Image source was not specified. Call fromRes() or fromBitmap() first.")

        transformations.forEach { transform ->
            bitmap = transform(bitmap)
        }
        return bitmap
    }

    fun saveBitmapToCache(bitmap: Bitmap): String? {
        return try {
            val file = File(context.cacheDir, FILE_COVER_ART_NAME)
            val stream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun Bitmap.createRoundedBitmap(cornerRadius: Float = 52f): Bitmap {
        val output = createBitmap(this.width, this.height)
        val canvas = Canvas(output)

        val paint = Paint()
        val rect = Rect(0, 0, this.width, this.height)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.BLACK
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, rect, rect, paint)

        return output
    }

    companion object {
        private const val WIDGET_IMAGE_SIZE_PX = 512
        private const val FILE_COVER_ART_NAME = "widget_cover_art.png"
    }
}