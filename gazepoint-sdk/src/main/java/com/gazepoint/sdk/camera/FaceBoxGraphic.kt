package com.gazepoint.sdk.camera

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.face.Face

/**
 * White outline around a detected face — including extra faces when more
 * than one person is in frame.
 */
class FaceBoxGraphic(
    overlay: GraphicOverlay,
    private val face: Face
) : GraphicOverlay.Graphic(overlay) {

    private val boxPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = BOX_STROKE_WIDTH
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(calculateRect(face.boundingBox), boxPaint)
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5f
    }
}
