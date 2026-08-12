# GazePoint SDK — Android Example

Demo host app that lives in this repository and compiles against the local `:gazepoint-sdk` module.

```
GazePointSDK-Android/
├── gazepoint-sdk/     # library (JitPack artifact)
└── example/           # this app
```

## Open in Android Studio

1. **File → Open** → select `example/` (this folder)
2. Wait for Gradle sync
3. Run the `app` configuration on a device with a camera

From the command line (inside `example/`):

```bash
./gradlew :app:assembleDebug
```

## What it shows

- Live front-camera preview (CameraX)
- Face bounding box overlay
- Gaze indicator from `GazeTracker`
- Confidence, blink, and head pose

## Requirements

- Android Studio / AGP 9.2+
- Device or emulator, API 24+
- Camera permission (requested at runtime)

Releasing the Android SDK does **not** require changes in Flutter or other platform repos. Tag this repository (for example `2.1.1`) and JitPack builds `com.github.Tareq-Ghassan:GazePointSDK-Android`.
