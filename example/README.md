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

- Live camera preview from the SDK (`GazePreviewView`)
- White outline on every detected face, aligned to the face
- Status **Multiple faces detected** when more than one face is in frame (gaze is not calculated in that case)
- **Flip camera** (front / back)
- Gaze indicator, confidence, blink, and head pose from `GazeCamera`

Metrics-only apps can use `GazeCamera` with `previewEnabled = false` and never attach a preview view.

## Requirements

- Android Studio / AGP 9.2+
- Device or emulator, API 24+
- Camera permission (requested at runtime)

Releasing the Android SDK does **not** require changes in Flutter or other platform repos. Tag this repository (for example `2.2.0`) and JitPack builds `com.github.Tareq-Ghassan:GazePointSDK-Android`.
