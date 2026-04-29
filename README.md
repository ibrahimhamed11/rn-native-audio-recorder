# rn-native-audio-recorder

By **Ibrahim Hamed** · [GitHub](https://github.com/ibrahimhamed11)

A React Native native module for **audio recording and playback** using platform APIs — `AVFoundation` on iOS and `MediaRecorder`/`MediaPlayer` on Android. Zero JS audio dependencies. Pure native performance.

**v1.1.1** updates screenshots. **v1.1.0** adds `preloadAudio` for zero-latency playback and `getRecordingLevel` for real-time waveform metering.

---

## Screenshots

<p align="center">
  <img src="https://raw.githubusercontent.com/ibrahimhamed11/rn-native-audio-recorder/main/screenshots/recording.png" width="200" alt="Recording in progress" />
  &nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/ibrahimhamed11/rn-native-audio-recorder/main/screenshots/ready-to-play.png" width="200" alt="Ready to play" />
  &nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/ibrahimhamed11/rn-native-audio-recorder/main/screenshots/playing.png" width="200" alt="Playing at 1.5x speed" />
</p>

<p align="center">
  <b>Recording</b> · <b>Ready to Play</b> · <b>Playing (1.5x speed)</b>
</p>

---

## Why This Exists

### The Problem

Most React Native audio libraries fall into two categories:

1. **JS-based wrappers** that use Web Audio APIs or Expo modules — they add heavy dependencies, don't support background recording well, and have inconsistent behavior across platforms.

2. **Bloated native libraries** like `react-native-audio-recorder-player` that include features you don't need (waveforms, metering, streaming) and drag in large native dependencies.

If you just need **simple, reliable voice recording and playback** (e.g. voice notes in a chat or notes app), you're forced to install a 500KB+ dependency with dozens of transitive native deps.

### The Challenge

- `AVAudioRecorder` (iOS) and `MediaRecorder` (Android) have completely different APIs
- File format compatibility across platforms (M4A/AAC works on both)
- Audio session management (playback category, speaker routing)
- Playback speed control requires different APIs per platform (`AVAudioPlayer.rate` vs `PlaybackParams`)
- Proper cleanup on component unmount / app backgrounding
- Android 12+ requires `MediaRecorder(Context)` constructor (API 31+)

### The Solution

This module provides a **thin, focused native bridge** that:

✅ Records audio to `.m4a` (AAC) — works on both platforms  
✅ Plays back with fine-grained speed control (0.5x, 0.75x, 1x, 1.25x, 1.5x, 1.75x, 2.0x) — powered by `AVAudioPlayer.rate` on iOS and `PlaybackParams` on Android  
✅ Supports seek, position tracking, and duration  
✅ Handles audio session setup automatically  
✅ Handles Android 12+ MediaRecorder changes  
✅ Cleans up resources on destroy  
✅ No third-party native dependencies — just platform APIs  
✅ Works with React Native 0.72+  

---

## Installation

```bash
npm install rn-native-audio-recorder
# or
yarn add rn-native-audio-recorder
```

---

## Android Setup

### 1. Register the package in `MainApplication.kt`:

```kotlin
import com.rnnativeaudiorecorder.AudioRecorderPackage

override fun getPackages(): List<ReactPackage> = PackageList(this).packages.apply {
    add(AudioRecorderPackage())
}
```

### 2. Add microphone permission to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

### 3. Copy native files

Copy `android/src/main/java/com/rnnativeaudiorecorder/AudioRecorderModule.kt` and `AudioRecorderPackage.kt` into your Android source directory (e.g. `android/app/src/main/java/com/rnnativeaudiorecorder/`).

---

## iOS Setup

### 1. Add to your Podfile:

```ruby
pod 'rn-native-audio-recorder', :path => '../node_modules/rn-native-audio-recorder'
```

Then run `pod install`.

### 2. Add microphone usage description to `Info.plist`:

```xml
<key>NSMicrophoneUsageDescription</key>
<string>This app needs microphone access to record voice notes.</string>
```

### 3. Copy native files

Copy `ios/AudioRecorder.swift` and `ios/AudioRecorder.m` into your Xcode project. Make sure to:
- Add them to your app target (check **Target Membership** in Xcode)
- If you don't have a bridging header, create one with:

```objc
#import <React/RCTBridgeModule.h>
```

---

## Usage

```typescript
import {
  startRecording,
  stopRecording,
  startPlaying,
  stopPlaying,
  preloadAudio,
  setPlaybackSpeed,
  getPlaybackPosition,
  seekTo,
  deleteRecording,
  getRecordingLevel,
  isAvailable,
} from 'rn-native-audio-recorder';

// Check if module is linked
console.log('AudioRecorder available:', isAvailable);

// Record
const { filePath } = await startRecording();

// Poll recording level (0..1) every 100ms to animate a waveform
const meter = setInterval(async () => {
  const level = await getRecordingLevel();
  console.log('Level:', level); // 0.0 - 1.0
}, 100);

// Stop recording
clearInterval(meter);
const { filePath: savedPath, duration } = await stopRecording();

// Preload for instant, zero-latency playback
await preloadAudio(savedPath);

// Play
const { duration: totalDuration } = await startPlaying(savedPath);

// Control playback
await setPlaybackSpeed(1.5);           // 1.5x speed
const pos = await getPlaybackPosition(); // { position, duration, isPlaying }
await seekTo(5000);                     // seek to 5 seconds
await stopPlaying();

// Cleanup
await deleteRecording(savedPath);
```

---

## API

### `isAvailable: boolean`
Whether the native module is linked and available at runtime.

### `startRecording(): Promise<StartRecordingResult>`
Start recording audio. Automatically requests microphone permission on Android.

Returns:
| Field | Type | Description |
|-------|------|-------------|
| `filePath` | `string` | Absolute path to the recording file |
| `success` | `boolean` | Whether recording started |

### `stopRecording(): Promise<StopRecordingResult>`
Stop the current recording.

Returns:
| Field | Type | Description |
|-------|------|-------------|
| `filePath` | `string` | Path to the recorded file |
| `duration` | `number` | Recording duration in milliseconds |
| `success` | `boolean` | Whether stop succeeded |

### `startPlaying(path: string): Promise<StartPlayingResult>`
Start playing an audio file.

| Param | Type | Description |
|-------|------|-------------|
| `path` | `string` | Absolute path to the audio file |

### `stopPlaying(): Promise<boolean>`
Stop playback.

### `setPlaybackSpeed(speed: number): Promise<boolean>`
Set playback speed. Common values: `0.5`, `1.0`, `1.5`, `2.0`.

### `getPlaybackPosition(): Promise<PlaybackPosition>`
Get current playback state.

Returns:
| Field | Type | Description |
|-------|------|-------------|
| `position` | `number` | Current position in ms |
| `duration` | `number` | Total duration in ms |
| `isPlaying` | `boolean` | Whether audio is playing |

### `seekTo(positionMs: number): Promise<boolean>`
Seek to a position in milliseconds.

### `deleteRecording(path: string): Promise<boolean>`
Delete a recording file from disk.

### `preloadAudio(path: string): Promise<PreloadAudioResult>`
Pre-buffers an audio file without playing it. Use this before `startPlaying` to eliminate startup latency (particularly useful in list-based UIs like chat voice notes).

Returns:
| Field | Type | Description |
|-------|------|-------------|
| `duration` | `number` | Total duration of the file in milliseconds |
| `success` | `boolean` | Whether preloading succeeded |

### `getRecordingLevel(): Promise<number>`
While recording is active, returns the current instantaneous amplitude normalized to `0..1`. Poll on an interval (e.g. every 100ms) to drive a live waveform or meter animation.

### `requestMicrophonePermission(): Promise<boolean>`
Manually request microphone permission (called automatically by `startRecording` on Android).

---

## Types

```typescript
interface StartRecordingResult {
  filePath: string;
  success: boolean;
}

interface StopRecordingResult {
  filePath: string;
  duration: number;
  success: boolean;
}

interface StartPlayingResult {
  duration: number;
  success: boolean;
}

interface PreloadAudioResult {
  duration: number;
  success: boolean;
}

interface PlaybackPosition {
  position: number;
  duration: number;
  isPlaying: boolean;
}
```

---

## Changelog

### v1.1.1
- ✅ Updated screenshots with latest UI

### v1.1.0
- ✅ Added `preloadAudio(path)` — pre-buffers audio for zero-latency playback
- ✅ Added `getRecordingLevel()` — real-time amplitude metering (0..1) for waveform UI
- ✅ Exported `PreloadAudioResult` type
- ✅ Improved JSDoc comments on all exported functions

---

## How It Works

```
User taps record → JS calls NativeModule → Kotlin MediaRecorder / Swift AVAudioRecorder
                                                    ↓
                                          Records to .m4a (AAC)
                                          in app cache/temp directory

User taps play   → JS calls NativeModule → Kotlin MediaPlayer / Swift AVAudioPlayer
                                                    ↓
                                          Plays from file path
                                          with speed control & seek
```

### Audio Format
- **Container**: MPEG-4 (`.m4a`)
- **Codec**: AAC
- **Sample Rate**: 44,100 Hz
- **Bit Rate**: 128 kbps
- **Channels**: Mono (1)

This format is chosen because it works natively on both Android and iOS with no transcoding needed.

---

## Common Errors

| Error | Cause | Fix |
|-------|-------|-----|
| `AudioRecorder native module not available` | Native files not linked | Run `pod install` (iOS) or register `AudioRecorderPackage` (Android) |
| `Microphone permission denied` | User denied permission | Show a rationale and re-request, or guide user to Settings |
| `RECORD_ERROR` | Recording failed to start | Check if another app is using the mic |
| `PLAY_ERROR` | File not found or corrupt | Verify the file path exists |
| `STOP_ERROR: No active recording` | `stopRecording` called without `startRecording` | Ensure you call `startRecording` first |

---

## Platform Notes

### Android
- Uses `MediaRecorder` for recording and `MediaPlayer` for playback
- Android 12+ (API 31): Uses the new `MediaRecorder(Context)` constructor
- Playback speed requires Android 6.0+ (API 23)
- Files stored in app cache directory

### iOS
- Uses `AVAudioRecorder` for recording and `AVAudioPlayer` for playback
- Audio session is set to `.playAndRecord` during recording, `.playback` during playback
- Speaker routing is enabled by default (`.defaultToSpeaker`)
- Files stored in temporary directory

---

## License

MIT — **Ibrahim Hamed**

🌐 [ibrahimtoulba.com](https://ibrahimtoulba.com) · 🐙 [github.com/ibrahimhamed11](https://github.com/ibrahimhamed11)
