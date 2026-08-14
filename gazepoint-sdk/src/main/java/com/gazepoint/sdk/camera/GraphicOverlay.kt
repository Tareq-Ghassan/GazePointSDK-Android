package com.gazepoint.sdk.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.camera.core.CameraSelector
import kotlin.math.max

/**
 * Overlay that draws a tight white outline on each detected face.
 */
class GraphicOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val lock = Any()
    private val graphics = mutableListOf<Graphic>()
    private var cameraSelector = CameraSelector.LENS_FACING_FRONT
    private var imageWidth = 1
    private var imageHeight = 1

    fun isFrontMode(): Boolean = cameraSelector == CameraSelector.LENS_FACING_FRONT

    fun setFrontFacing(front: Boolean) {
        cameraSelector = if (front) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
    }

    /** Upright image size that ML Kit bounding boxes are expressed in. */
    fun setImageSourceInfo(width: Int, height: Int) {
        imageWidth = width.coerceAtLeast(1)
        imageHeight = height.coerceAtLeast(1)
    }

    fun clear() {
        synchronized(lock) {
            graphics.clear()
        }
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        synchronized(lock) {
            graphics.add(graphic)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }

    abstract class Graphic(private val overlay: GraphicOverlay) {
        abstract fun draw(canvas: Canvas)

        fun calculateRect(boundingBox: Rect): RectF {
            val viewW = overlay.width.toFloat().coerceAtLeast(1f)
            val viewH = overlay.height.toFloat().coerceAtLeast(1f)
            val imageW = overlay.imageWidth.toFloat()
            val imageH = overlay.imageHeight.toFloat()
            val scale = max(viewW / imageW, viewH / imageH)
            val drawnW = imageW * scale
            val drawnH = imageH * scale
            val ox = (viewW - drawnW) / 2f
            val oy = (viewH - drawnH) / 2f

            var left = boundingBox.left * scale + ox
            var top = boundingBox.top * scale + oy
            var right = boundingBox.right * scale + ox
            var bottom = boundingBox.bottom * scale + oy

            if (overlay.isFrontMode()) {
                val mirroredLeft = viewW - right
                val mirroredRight = viewW - left
                left = mirroredLeft
                right = mirroredRight
            }

            val rect = RectF(left, top, right, bottom)
            val insetX = rect.width() * 0.08f
            val insetY = rect.height() * 0.06f
            rect.inset(insetX, insetY)
            return rect
        }
    }
}
