package com.facedetection.math

import kotlin.math.sqrt

/**
 * Lightweight 3D vector used for gaze math.
 * Replaces the abandoned Sceneform Vector3 (which shipped non-16KB-aligned native code).
 */
data class Vector3(
    @JvmField var x: Float = 0f,
    @JvmField var y: Float = 0f,
    @JvmField var z: Float = 0f
) {
    fun normalized(): Vector3 {
        val magnitude = sqrt(x * x + y * y + z * z)
        if (magnitude == 0f) return Vector3(0f, 0f, 0f)
        return Vector3(x / magnitude, y / magnitude, z / magnitude)
    }
}
