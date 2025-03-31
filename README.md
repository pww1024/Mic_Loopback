![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

# 🎧 MicLoopback - Android Microphone Loopback Tool

> A lightweight Android app that provides real-time microphone input loopback to the speaker, enabling **live ear monitoring**. Ideal for developers doing audio testing, microphone debugging, or latency experiments.

---

## 🧩 Features

- Real-time microphone to speaker loopback
- Low-latency audio pipeline using `AudioRecord` + `AudioTrack`
- Runs as a foreground service to avoid being killed in the background
- Compatible with Android 8.0+ foreground service requirements
- Simple Jetpack Compose UI for control interface

---

## 🚀 Getting Started

### Required Permissions

The app requires microphone recording permission:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

Runtime permission request is also handled for Android 6.0+ devices.

---

### How to Run

1. Clone the repository:

```bash
git clone https://github.com/yourusername/MicLoopback.git
cd MicLoopback
```

2. Open the project in **Android Studio**.
3. Connect a device or start an emulator.
4. Click **Run** to launch the app.
5. Grant microphone permission and tap the **Start Loopback** button to begin.

---

## ⚙️ How It Works

- Uses `AudioRecord` to capture microphone input.
- Streams the audio directly to the speaker via `AudioTrack`.
- A background thread handles audio transfer to avoid UI blocking.
- Runs as a foreground service with a persistent notification.

---

## 📂 Project Structure

- `MainActivity.kt` – Main UI and runtime permission logic.
- `LoopbackService.kt` – Foreground service handling audio recording/playback.
- `AndroidManifest.xml` – Permission and service declarations.

---

## ⚠️ Known Issues

- On some devices, echo or noise may occur due to hardware limitations.
- Current setup uses mono audio and PCM 16-bit. You can customize sample rate or channels as needed.

---

## 🧰 Development Environment

- Android Studio Giraffe / Hedgehog
- Kotlin + Jetpack Compose
- Min SDK: 24
