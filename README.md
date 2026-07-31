# GazePoint SDK for Android

Advanced eye tracking and gaze point detection SDK for Android applications using Google ML Kit.

**Repository:** [Tareq-Ghassan/GazePointSDK-Android](https://github.com/Tareq-Ghassan/GazePointSDK-Android)  
**Umbrella monorepo:** [FaceDetection-GazePoint](https://github.com/Tareq-Ghassan/FaceDetection-GazePoint)

## Features

- ✅ **Real-time Gaze Tracking** — Track the user's gaze point on screen in real time
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

### Local module (recommended while developing)

In your app's `settings.gradle`:

```gradle
include ':gazepoint-sdk'
project(':gazepoint-sdk').projectDir = new File(settingsDir, '../android/gazepoint-sdk')
```

In your app's `build.gradle`:

```gradle
dependencies {
    implementation project(':gazepoint-sdk')
}
```

See the [`android_example`](../android_example) sample for a complete host app.

### JitPack (after tagging a release)

1. Push `main` and tag `2.0.0` on [GazePointSDK-Android](https://github.com/Tareq-Ghassan/GazePointSDK-Android)
2. Open [jitpack.io/#Tareq-Ghassan/GazePointSDK-Android](https://jitpack.io/#Tareq-Ghassan/GazePointSDK-Android) and build the tag
3. Consume:

```gradle
// settings.gradle / dependencyResolutionManagement
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Tareq-Ghassan:GazePointSDK-Android:2.0.0'
}
```

This repo includes `jitpack.yml` + `maven-publish` on `:gazepoint-sdk` so JitPack can build the library module.

## Quick Start

### 1. Camera permission

In your app `AndroidManifest.xml`:

```xml
<uses-feature android:name="android.hardware.camera.any" />
<uses-permission android:name="android.permission.CAMERA" />
```

### 2. Basic usage

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

### 3. Calibration

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

Open [`android_example`](../android_example) in Android Studio. It depends on this library via a local Gradle project reference (same idea as `ios_example` → `ios`).

## License

MIT License — see the [LICENSE](https://github.com/Tareq-Ghassan/FaceDetection-GazePoint/blob/main/LICENSE) file.

## Support

- Issues: [GazePointSDK-Android](https://github.com/Tareq-Ghassan/GazePointSDK-Android/issues)
- Umbrella: [FaceDetection-GazePoint](https://github.com/Tareq-Ghassan/FaceDetection-GazePoint/issues)
