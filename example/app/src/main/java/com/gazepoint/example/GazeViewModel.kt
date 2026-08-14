package com.gazepoint.example

import android.graphics.PointF
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gazepoint.sdk.camera.GazeFrame

/**
 * Holds the latest [GazeFrame] from the SDK for the demo UI.
 */
class GazeViewModel : ViewModel() {

    data class UiState(
        val faceDetected: Boolean = false,
        val statusText: String = "Starting camera…",
        val gazePoint: PointF? = null,
        val confidence: Float = 0f,
        val isBlinking: Boolean = false,
        val pitch: Float = 0f,
        val yaw: Float = 0f,
        val roll: Float = 0f
    )

    private val _uiState = MutableLiveData(UiState())
    val uiState: LiveData<UiState> = _uiState

    fun applyFrame(frame: GazeFrame) {
        val gaze = frame.gaze
        _uiState.postValue(
            UiState(
                faceDetected = frame.faceDetected,
                statusText = frame.statusText,
                gazePoint = gaze?.gazePoint,
                confidence = gaze?.confidence ?: 0f,
                isBlinking = gaze?.isBlinking ?: false,
                pitch = gaze?.headPose?.pitch ?: 0f,
                yaw = gaze?.headPose?.yaw ?: 0f,
                roll = gaze?.headPose?.roll ?: 0f
            )
        )
    }

    fun setStatus(message: String) {
        val current = _uiState.value ?: UiState()
        _uiState.postValue(current.copy(statusText = message, faceDetected = false))
    }
}
