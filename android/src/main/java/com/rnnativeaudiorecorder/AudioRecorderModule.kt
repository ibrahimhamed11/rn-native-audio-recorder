package com.rnnativeaudiorecorder

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import com.facebook.react.bridge.*
import java.io.File

class AudioRecorderModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName() = "AudioRecorder"

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var filePath: String? = null
    private var recordingStartTime: Long = 0

    @ReactMethod
    fun startRecording(promise: Promise) {
        try {
            val file = File(reactApplicationContext.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            filePath = file.absolutePath

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(reactApplicationContext)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(filePath)
                prepare()
                start()
            }

            recordingStartTime = System.currentTimeMillis()
            val result = Arguments.createMap()
            result.putString("filePath", filePath)
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("RECORD_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun stopRecording(promise: Promise) {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null

            val duration = System.currentTimeMillis() - recordingStartTime
            val result = Arguments.createMap()
            result.putString("filePath", filePath)
            result.putDouble("duration", duration.toDouble())
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            recorder = null
            promise.reject("STOP_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun startPlaying(path: String, promise: Promise) {
        try {
            stopPlayingInternal()

            player = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
            }

            val result = Arguments.createMap()
            result.putDouble("duration", player?.duration?.toDouble() ?: 0.0)
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("PLAY_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun stopPlaying(promise: Promise) {
        try {
            stopPlayingInternal()
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("STOP_PLAY_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun setPlaybackSpeed(speed: Float, promise: Promise) {
        try {
            player?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val params = it.playbackParams
                    params.speed = speed
                    it.playbackParams = params
                }
            }
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("SPEED_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getPlaybackPosition(promise: Promise) {
        try {
            val result = Arguments.createMap()
            result.putDouble("position", player?.currentPosition?.toDouble() ?: 0.0)
            result.putDouble("duration", player?.duration?.toDouble() ?: 0.0)
            result.putBoolean("isPlaying", player?.isPlaying ?: false)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("POSITION_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun seekTo(positionMs: Double, promise: Promise) {
        try {
            player?.seekTo(positionMs.toInt())
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("SEEK_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun deleteRecording(path: String, promise: Promise) {
        try {
            val file = File(path)
            if (file.exists()) file.delete()
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("DELETE_ERROR", e.message, e)
        }
    }

    private fun stopPlayingInternal() {
        player?.apply {
            if (isPlaying) stop()
            release()
        }
        player = null
    }

    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        try {
            recorder?.release()
            player?.release()
        } catch (_: Exception) {}
        recorder = null
        player = null
    }
}
