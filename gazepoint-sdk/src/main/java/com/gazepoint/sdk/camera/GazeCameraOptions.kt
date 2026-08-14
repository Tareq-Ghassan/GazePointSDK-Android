package com.gazepoint.sdk.camera

import com.gazepoint.sdk.GazeTracker

/**
 * One processed camera frame from [GazeCamera].
 *
 * [statusText] is the SDK message for the host UI:
 * "No face detected", "Multiple faces detected", "Blink detected", or "Tracking".
 */
data class GazeFrame(
    val gaze: GazeTracker.GazeResult?,
    val faceCount: Int,
    val faceDetected: Boolean,
    val statusText: String
) {
    val hasMultipleFaces: Boolean get() = faceCount > 1
}

/**
 * Options for [GazeCamera].
 *
 * Tracking (metrics) always runs. Preview and face boxes are opt-in.
 */
data class GazeCameraOptions(
    /** Bind a live [GazePreviewView]. False = headless metrics only. */
    val previewEnabled: Boolean = true,
    /** Draw a white rectangle around every detected face. */
    val showFaceBoxes: Boolean = true
)
