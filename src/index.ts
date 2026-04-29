/**
 * rn-native-audio-recorder
 * React Native native module for audio recording & playback
 * Supports: startRecording, stopRecording, startPlaying, stopPlaying,
 *           preloadAudio, seekTo, getPlaybackPosition, setPlaybackSpeed,
 *           deleteRecording, getRecordingLevel
 * By Ibrahim Hamed · https://ibrahimtoulba.com
 */

import { NativeModules, Platform, PermissionsAndroid } from 'react-native';

const { AudioRecorder: NativeAudioRecorder } = NativeModules;

// ── Types ──────────────────────────────────────────────

export interface StartRecordingResult {
  filePath: string;
  success: boolean;
}

export interface StopRecordingResult {
  filePath: string;
  duration: number;
  success: boolean;
}

export interface StartPlayingResult {
  duration: number;
  success: boolean;
}

export interface PlaybackPosition {
  position: number;
  duration: number;
  isPlaying: boolean;
}

export interface PreloadAudioResult {
  duration: number;
  success: boolean;
}

export interface RecordingLevel {
  level: number; // 0..1
}

// ── Permission helper ──────────────────────────────────

export async function requestMicrophonePermission(): Promise<boolean> {
  if (Platform.OS === 'android') {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
      {
        title: 'Microphone Permission',
        message: 'This app needs microphone access to record audio.',
        buttonPositive: 'OK',
        buttonNegative: 'Cancel',
      },
    );
    return granted === PermissionsAndroid.RESULTS.GRANTED;
  }
  // iOS permission is handled automatically by AVAudioSession
  return true;
}

// ── Guard ──────────────────────────────────────────────

function assertAvailable(): void {
  if (!NativeAudioRecorder) {
    throw new Error(
      'rn-native-audio-recorder: NativeModule "AudioRecorder" is not linked. ' +
      'Did you run pod install (iOS) or add AudioRecorderPackage (Android)?',
    );
  }
}

// ── Public API ─────────────────────────────────────────

/** Whether the native module is linked and available. */
export const isAvailable: boolean = !!NativeAudioRecorder;

/**
 * Start recording audio to a temporary `.m4a` file.
 * Automatically requests microphone permission on Android.
 */
export async function startRecording(): Promise<StartRecordingResult> {
  assertAvailable();
  const hasPermission = await requestMicrophonePermission();
  if (!hasPermission) {
    throw new Error('Microphone permission denied');
  }
  return NativeAudioRecorder.startRecording();
}

/** Stop the current recording. Returns file path and duration in ms. */
export async function stopRecording(): Promise<StopRecordingResult> {
  assertAvailable();
  return NativeAudioRecorder.stopRecording();
}

/** Start playing an audio file at the given path. */
export async function startPlaying(path: string): Promise<StartPlayingResult> {
  assertAvailable();
  return NativeAudioRecorder.startPlaying(path);
}

/** Stop playback. */
export async function stopPlaying(): Promise<boolean> {
  assertAvailable();
  return NativeAudioRecorder.stopPlaying();
}

/** Set playback speed (e.g. 0.5, 1.0, 1.5, 2.0). */
export async function setPlaybackSpeed(speed: number): Promise<boolean> {
  assertAvailable();
  return NativeAudioRecorder.setPlaybackSpeed(speed);
}

/** Get current playback position, duration, and playing state. */
export async function getPlaybackPosition(): Promise<PlaybackPosition> {
  assertAvailable();
  return NativeAudioRecorder.getPlaybackPosition();
}

/** Seek to a position in milliseconds. */
export async function seekTo(positionMs: number): Promise<boolean> {
  assertAvailable();
  return NativeAudioRecorder.seekTo(positionMs);
}

/** Delete a recording file from disk. */
export async function deleteRecording(path: string): Promise<boolean> {
  assertAvailable();
  return NativeAudioRecorder.deleteRecording(path);
}

/**
 * Preload an audio file for instant playback (pre-buffers without playing).
 * Call this before `startPlaying` to eliminate startup latency.
 * Returns duration in milliseconds.
 */
export async function preloadAudio(path: string): Promise<PreloadAudioResult> {
  assertAvailable();
  return NativeAudioRecorder.preloadAudio(path);
}

/**
 * While recording, returns the current instantaneous amplitude level normalized to 0..1.
 * Poll this on an interval (e.g. every 100ms) to drive a waveform / meter UI.
 */
export async function getRecordingLevel(): Promise<number> {
  assertAvailable();
  return NativeAudioRecorder.getRecordingLevel();
}
