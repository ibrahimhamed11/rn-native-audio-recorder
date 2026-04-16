import Foundation
import AVFoundation

@objc(AudioRecorder)
class AudioRecorder: NSObject {

  private var recorder: AVAudioRecorder?
  private var player: AVAudioPlayer?
  private var filePath: String?
  private var recordingStartTime: Date?

  @objc
  func startRecording(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
    let session = AVAudioSession.sharedInstance()
    do {
      try session.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker])
      try session.setActive(true)

      let fileName = "voice_\(Int(Date().timeIntervalSince1970 * 1000)).m4a"
      let url = FileManager.default.temporaryDirectory.appendingPathComponent(fileName)
      filePath = url.path

      let settings: [String: Any] = [
        AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
        AVSampleRateKey: 44100,
        AVNumberOfChannelsKey: 1,
        AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue,
        AVEncoderBitRateKey: 128000
      ]

      recorder = try AVAudioRecorder(url: url, settings: settings)
      recorder?.record()
      recordingStartTime = Date()

      resolve(["filePath": filePath!, "success": true])
    } catch {
      reject("RECORD_ERROR", error.localizedDescription, error)
    }
  }

  @objc
  func stopRecording(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
    guard let recorder = recorder else {
      reject("STOP_ERROR", "No active recording", nil)
      return
    }

    recorder.stop()
    let duration = Date().timeIntervalSince(recordingStartTime ?? Date()) * 1000
    self.recorder = nil

    resolve(["filePath": filePath ?? "", "duration": duration, "success": true])
  }

  @objc
  func startPlaying(_ path: String, resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
    do {
      let session = AVAudioSession.sharedInstance()
      try session.setCategory(.playback, mode: .default)
      try session.setActive(true)

      let url = URL(fileURLWithPath: path)
      player = try AVAudioPlayer(contentsOf: url)
      player?.enableRate = true
      player?.play()

      resolve(["duration": (player?.duration ?? 0) * 1000, "success": true])
    } catch {
      reject("PLAY_ERROR", error.localizedDescription, error)
    }
  }

  @objc
  func stopPlaying(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
    player?.stop()
    player = nil
    resolve(true)
  }

  @objc
  func setPlaybackSpeed(_ speed: Float, resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
    player?.rate = speed
    resolve(true)
  }

  @objc
  func getPlaybackPosition(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
    resolve([
      "position": (player?.currentTime ?? 0) * 1000,
      "duration": (player?.duration ?? 0) * 1000,
      "isPlaying": player?.isPlaying ?? false
    ])
  }

  @objc
  func seekTo(_ positionMs: Double, resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
    player?.currentTime = positionMs / 1000.0
    resolve(true)
  }

  @objc
  func deleteRecording(_ path: String, resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
    do {
      if FileManager.default.fileExists(atPath: path) {
        try FileManager.default.removeItem(atPath: path)
      }
      resolve(true)
    } catch {
      reject("DELETE_ERROR", error.localizedDescription, error)
    }
  }
}
