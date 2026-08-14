# GazePoint SDK for Android

Advanced eye tracking and gaze point detection SDK for Android applications using Google ML Kit.

**Repository:** [Tareq-Ghassan/GazePointSDK-Android](https://github.com/Tareq-Ghassan/GazePointSDK-Android)  
**Umbrella monorepo:** [FaceDetection-GazePoint](https://github.com/Tareq-Ghassan/FaceDetection-GazePoint)

## Features

- ✅ **Live camera preview** — Opt-in `GazePreviewView` (disable for metrics-only apps)
- ✅ **Face bounding boxes** — White outline on every detected face (aligned to the preview)
- ✅ **Multi-face status** — `GazeFrame.statusText` is `"Multiple faces detected"` when more than one face is in frame; `frame.gaze` is `null` until only one face remains
- ✅ **Real-time Gaze Tracking** — Track the user's gaze point on screen in real time (single face only)
- ✅ **Head Pose Compensation** — Accurate tracking regardless of head position
- ✅ **Blink Detection** — Detect blinks using eye-open probability / EAR
- ✅ **Kalman Filtering** — Smooth gaze point tracking
- ✅ **Adaptive Smoothing** — Velocity-based smoothing for natural movement
- ✅ **Calibration Support** — Multi-point calibration for improved accuracy
- ✅ **Performance Monitoring** — Built-in FPS and processing time tracking

## Requirements

- Android API 24+ (Android 7.0)
- Kotlin / Java host app
- Device with a front-facing camera
- Camera permission

## Installation

### Local module (this repository)

This repo is already a Gradle project. Open [`example/`](example/) in Android Studio to run the demo. External apps should use JitPack (below) or:

```gradle
include ':gazepoint-sdk'
project(':gazepoint-sdk').projectDir = new File(settingsDir, '../gazepoint-sdk')
```

### JitPack (after tagging a release)

1. Push `main` and tag `2.2.0` on [GazePointSDK-Android](https://github.com/Tareq-Ghassan/GazePointSDK-Android)
2. Open [jitpack.io/#Tareq-Ghassan/GazePointSDK-Android](https://jitpack.io/#Tareq-Ghassan/GazePointSDK-Android) and build the tag
3. Consume:

```gradle
// settings.gradle / dependencyResolutionManagement
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Tareq-Ghassan:GazePointSDK-Android:2.2.0'
}
```

This repo includes `jitpack.yml` + `maven-publish` on `:gazepoint-sdk` so JitPack can build the library module.

Use tag **2.2.0** for preview/`GazeCamera`. Flutter `gazepoint_sdk` 3.0.4+ depends on 2.2.0. Tag `2.1.0` never built on JitPack.

## Quick Start

### 1. Camera permission

In your app `AndroidManifest.xml`:

```xml
<uses-feature android:name="android.hardware.camera.any" />
<uses-permission android:name="android.permission.CAMERA" />
```

### 2. Camera + preview (recommended)

```kotlin
import com.gazepoint.sdk.camera.GazeCamera
import com.gazepoint.sdk.camera.GazeCameraOptions
import com.gazepoint.sdk.camera.GazePreviewView

val camera = GazeCamera(this) { frame ->
    // frame.statusText — "No face detected" | "Multiple faces detected" | "Blink detected" | "Tracking"
    // frame.gaze — null unless exactly one face is in frame
    // White boxes are drawn by the SDK on GazePreviewView when showFaceBoxes is true
}

camera.configure(GazeCameraOptions(previewEnabled = true, showFaceBoxes = true))
camera.attachPreview(binding.gazePreview) // <com.gazepoint.sdk.camera.GazePreviewView>
camera.start(this) // LifecycleOwner

// Metrics only: GazeCameraOptions(previewEnabled = false) and skip attachPreview()
```

### 3. Gaze math only (you already have an ML Kit `Face`)

```kotlin
import com.gazepoint.sdk.GazeTracker
import com.google.mlkit.vision.face.Face

val gazeTracker = GazeTracker(context)

fun onFaceDetected(face: Face) {
    val result = gazeTracker.calculateGazePoint(face) ?: return
    // result.gazePoint  — screen coordinates
    // result.confidence — 0f..1f
    // result.isBlinking
    // result.headPose.pitch / yaw / roll
}
```

### 4. Calibration

```kotlin
gazeTracker.calibrate(
    listOf(
        PointF(100f, 100f) to PointF(95f, 102f),
        PointF(375f, 100f) to PointF(378f, 98f),
        PointF(100f, 750f) to PointF(102f, 755f),
    )
)
```

### 4. Performance monitoring

```kotlin
val monitor = PerformanceMonitor()
val start = monitor.startFrame()
// ... process frame ...
monitor.endFrame(start)
val metrics = monitor.getMetrics()
```

## Sample app

Open [`example`](example/) in Android Studio (or `cd example && ./gradlew :app:assembleDebug`).

Pass: camera preview, white face boxes on the faces, **Multiple faces detected** with two people in frame and **no gaze point**, moving gaze indicator with one face, confidence > 0, blink flag. See [TESTING.md](https://github.com/Tareq-Ghassan/FaceDetection-GazePoint/blob/main/TESTING.md).

## License

MIT License — see the [LICENSE](https://github.com/Tareq-Ghassan/FaceDetection-GazePoint/blob/main/LICENSE) file.

## Support

- Issues: [GazePointSDK-Android](https://github.com/Tareq-Ghassan/GazePointSDK-Android/issues)
- Umbrella: [FaceDetection-GazePoint](https://github.com/Tareq-Ghassan/FaceDetection-GazePoint/issues)
