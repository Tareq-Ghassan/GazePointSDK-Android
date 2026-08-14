package com.gazepoint.sdk.camera

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.camera.view.PreviewView

/**
 * Live camera preview plus face-box overlay, owned by the Android SDK.
 *
 * Host apps (including Flutter via a PlatformView) attach this view when they
 * want a preview. Metrics still work if you never add it — see
 * [GazeCamera.previewEnabled].
 */
class GazePreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val previewView: PreviewView = PreviewView(context).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    val overlay: GraphicOverlay = GraphicOverlay(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    init {
        addView(previewView)
        addView(overlay)
    }
}
