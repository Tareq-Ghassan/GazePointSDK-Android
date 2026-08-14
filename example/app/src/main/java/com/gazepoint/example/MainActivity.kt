package com.gazepoint.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.gazepoint.example.databinding.ActivityMainBinding
import com.gazepoint.sdk.GazePointSDK
import com.gazepoint.sdk.camera.GazeCamera
import com.gazepoint.sdk.camera.GazeCameraOptions

/**
 * Demo host — camera preview, face boxes, and status come from the SDK.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: GazeViewModel
    private lateinit var gazeCamera: GazeCamera

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            gazeCamera.start(this)
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GazePointSDK.printInfo()

        viewModel = ViewModelProvider(this)[GazeViewModel::class.java]
        gazeCamera = GazeCamera(this) { frame ->
            viewModel.applyFrame(frame)
        }
        gazeCamera.configure(GazeCameraOptions(previewEnabled = true, showFaceBoxes = true))
        gazeCamera.attachPreview(binding.gazePreview)

        binding.btnSwitch.setOnClickListener { gazeCamera.switchCamera() }

        viewModel.uiState.observe(this) { state ->
            binding.statusText.text = state.statusText
            binding.statusText.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (state.faceDetected && !state.statusText.startsWith("Multiple")) {
                        android.R.color.holo_green_light
                    } else {
                        android.R.color.holo_orange_light
                    }
                )
            )

            if (state.gazePoint != null && state.faceDetected) {
                val point = state.gazePoint
                binding.gazePointText.text = getString(
                    R.string.gaze_detail_format,
                    point.x,
                    point.y,
                    state.confidence * 100f
                )
                binding.headPoseText.text = getString(
                    R.string.head_pose_format,
                    state.pitch,
                    state.yaw,
                    state.roll
                ) + if (state.isBlinking) "\nEyes: blinking" else "\nEyes: open"

                binding.gazeIndicator.visibility = View.VISIBLE
                binding.gazeIndicator.post {
                    val halfW = binding.gazeIndicator.width / 2f
                    val halfH = binding.gazeIndicator.height / 2f
                    binding.gazeIndicator.x = point.x - halfW
                    binding.gazeIndicator.y = point.y - halfH
                }
            } else {
                binding.gazePointText.text = if (state.faceDetected) {
                    "Gaze is only calculated when one face is in frame."
                } else {
                    getString(R.string.gaze_point_placeholder)
                }
                binding.headPoseText.text = if (state.faceDetected) "" else getString(R.string.point_camera_hint)
                binding.gazeIndicator.visibility = View.GONE
            }
        }

        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> gazeCamera.start(this)
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::gazeCamera.isInitialized) {
            gazeCamera.dispose()
        }
    }
}
