package com.gazepoint.sdk.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.gazepoint.sdk.GazeTracker
import com.gazepoint.sdk.PerformanceMonitor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors

/**
 * CameraX + ML Kit session owned by the Android SDK.
 *
 * Always produces [GazeFrame] metrics. Live preview and white face boxes are
 * opt-in via [options] / [previewEnabled] and an attached [GazePreviewView].
 */
class GazeCamera(
    context: Context,
    private val listener: (GazeFrame) -> Unit
) {
    private val appContext = context.applicationContext
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val gazeTracker = GazeTracker(appContext)
    private val performanceMonitor = PerformanceMonitor()
    private val faceDetector: FaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
    )

    private var cameraProvider: ProcessCameraProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var previewView: GazePreviewView? = null
    private var isRunning = false
    private var lensFacing = CameraSelector.LENS_FACING_FRONT

    var options: GazeCameraOptions = GazeCameraOptions()
        private set

    var previewEnabled: Boolean
        get() = options.previewEnabled
        set(value) {
            options = options.copy(previewEnabled = value)
            rebind()
        }

    var showFaceBoxes: Boolean
        get() = options.showFaceBoxes
        set(value) {
            options = options.copy(showFaceBoxes = value)
            if (!value) {
                previewView?.overlay?.clear()
            }
        }

    fun configure(options: GazeCameraOptions) {
        this.options = options
        rebind()
    }

    fun attachPreview(view: GazePreviewView) {
        previewView = view
        view.overlay.setFrontFacing(lensFacing == CameraSelector.LENS_FACING_FRONT)
        rebind()
    }

    fun detachPreview(view: GazePreviewView? = previewView) {
        if (view != null && previewView === view) {
            previewView?.overlay?.clear()
            previewView = null
            rebind()
        }
    }

    fun start(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        if (isRunning) {
            bindUseCases(lifecycleOwner)
            return
        }
        val future = ProcessCameraProvider.getInstance(appContext)
        future.addListener({
            try {
                cameraProvider = future.get()
                bindUseCases(lifecycleOwner)
                isRunning = true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start camera", e)
                listener(
                    GazeFrame(
                        gaze = null,
                        faceCount = 0,
                        faceDetected = false,
                        statusText = "Camera unavailable"
                    )
                )
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        previewView?.overlay?.setFrontFacing(lensFacing == CameraSelector.LENS_FACING_FRONT)
        rebind()
    }

    fun calibrate(points: List<Pair<PointF, PointF>>) {
        gazeTracker.calibrate(points)
    }

    fun resetCalibration() {
        gazeTracker.resetCalibration()
    }

    fun getPerformanceMetrics(): PerformanceMonitor.PerformanceMetrics {
        return performanceMonitor.getMetrics()
    }

    fun getLatestGaze(): GazeTracker.GazeResult? = latestGaze

    fun stop() {
        isRunning = false
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error unbinding camera", e)
        }
        previewView?.overlay?.clear()
    }

    fun dispose() {
        stop()
        try {
            faceDetector.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing face detector", e)
        }
        cameraExecutor.shutdown()
        previewView = null
        latestGaze = null
    }

    private var latestGaze: GazeTracker.GazeResult? = null

    private fun rebind() {
        val owner = lifecycleOwner
        if (isRunning && owner != null) {
            bindUseCases(owner)
        }
    }

    private fun bindUseCases(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        val provider = cameraProvider ?: return

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(cameraExecutor, ::analyzeFrame) }

        val preview = if (options.previewEnabled) {
            previewView?.let { view ->
                Preview.Builder().build().also { p ->
                    p.surfaceProvider = view.previewView.surfaceProvider
                }
            }
        } else {
            previewView?.overlay?.clear()
            null
        }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            provider.unbindAll()
            if (preview != null) {
                provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
            } else {
                provider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun analyzeFrame(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        val rotation = imageProxy.imageInfo.rotationDegrees
        val bufferW = imageProxy.width
        val bufferH = imageProxy.height
        val startTime = performanceMonitor.startFrame()

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                emit(faces, bufferW, bufferH, rotation)
                performanceMonitor.endFrame(startTime)
                imageProxy.close()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Face detection failed", e)
                latestGaze = null
                listener(
                    GazeFrame(
                        gaze = null,
                        faceCount = 0,
                        faceDetected = false,
                        statusText = "No face detected"
                    )
                )
                performanceMonitor.endFrame(startTime)
                imageProxy.close()
            }
    }

    private fun emit(faces: List<Face>, bufferWidth: Int, bufferHeight: Int, rotation: Int) {
        val overlay = previewView?.overlay
        overlay?.clear()

        val uprightWidth = if (rotation == 90 || rotation == 270) bufferHeight else bufferWidth
        val uprightHeight = if (rotation == 90 || rotation == 270) bufferWidth else bufferHeight
        overlay?.setImageSourceInfo(uprightWidth, uprightHeight)

        if (faces.isEmpty()) {
            latestGaze = null
            overlay?.postInvalidate()
            listener(
                GazeFrame(
                    gaze = null,
                    faceCount = 0,
                    faceDetected = false,
                    statusText = "No face detected"
                )
            )
            return
        }

        if (options.previewEnabled && options.showFaceBoxes && overlay != null) {
            for (face in faces) {
                overlay.add(FaceBoxGraphic(overlay, face))
            }
        }
        overlay?.postInvalidate()

        val primary = if (faces.size == 1) faces.first() else null
        val previewW = overlay?.width?.toFloat() ?: 0f
        val previewH = overlay?.height?.toFloat() ?: 0f
        val gaze = primary?.let {
            gazeTracker.calculateGazePoint(it, previewW, previewH)
        }
        if (faces.size != 1) {
            latestGaze = null
        } else {
            latestGaze = gaze
        }

        val status = when {
            faces.size > 1 -> "Multiple faces detected"
            gaze?.isBlinking == true -> "Blink detected"
            gaze != null -> "Tracking"
            else -> "No face detected"
        }

        listener(
            GazeFrame(
                gaze = gaze,
                faceCount = faces.size,
                faceDetected = true,
                statusText = status
            )
        )
    }

    companion object {
        private const val TAG = "GazeCamera"
    }
}
