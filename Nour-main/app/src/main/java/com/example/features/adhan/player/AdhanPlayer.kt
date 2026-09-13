package com.example.features.adhan.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

object AdhanPlayer {
    private const val TAG = "AdhanPlayer"
    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null

    var isPlaying: Boolean = false
        private set

    /**
     * Plays the selected Adhan recording or synthesizes sacred Adhan melodic tones.
     */
    fun playAdhan(
        context: Context,
        toneName: String = "Makkah Adhan",
        customUriString: String? = null,
        volume: Float = 1.0f,
        onCompletion: (() -> Unit)? = null
    ) {
        stop()

        if (!customUriString.isNullOrEmpty()) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(context, Uri.parse(customUriString))
                    setVolume(volume, volume)
                    setOnCompletionListener {
                        this@AdhanPlayer.isPlaying = false
                        onCompletion?.invoke()
                    }
                    prepare()
                    start()
                }
                isPlaying = true
                return
            } catch (e: Exception) {
                Log.e(TAG, "Error playing custom audio URI, falling back to melodic Adhan", e)
            }
        }

        // Built-in authentic harmonic synthesis (Maqam Hijaz: D4, Eb4, F#4, G4, A4, Bb4, C5)
        playHarmonicAdhan(toneName, volume, onCompletion)
    }

    private fun playHarmonicAdhan(
        toneName: String,
        volume: Float,
        onCompletion: (() -> Unit)?
    ) {
        playbackJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                isPlaying = true
                val sampleRate = 44100
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize * 4)
                    .build()

                audioTrack?.setVolume(volume)
                audioTrack?.play()

                // Notes in Maqam Hijaz (Frequencies in Hz)
                val d4 = 293.66
                val eb4 = 311.13
                val fs4 = 369.99
                val g4 = 392.00
                val a4 = 440.00
                val bb4 = 466.16
                val c5 = 523.25
                val d5 = 587.33

                // Phrase melody: "Allahu Akbar, Allahu Akbar" motif
                val melody = listOf(
                    // Al-la-hu Ak-bar
                    Triple(d4, 0.45, 0.8),
                    Triple(fs4, 0.45, 0.9),
                    Triple(g4, 0.70, 0.95),
                    Triple(a4, 1.10, 1.0),
                    Triple(g4, 0.50, 0.7),
                    Triple(fs4, 0.70, 0.6),
                    Triple(0.0, 0.35, 0.0), // rest

                    // Al-la-hu Ak-bar (Repetition)
                    Triple(d4, 0.45, 0.8),
                    Triple(fs4, 0.45, 0.9),
                    Triple(g4, 0.70, 0.95),
                    Triple(a4, 1.20, 1.0),
                    Triple(bb4, 0.60, 0.9),
                    Triple(a4, 0.80, 0.8),
                    Triple(g4, 0.70, 0.6),
                    Triple(0.0, 0.40, 0.0), // rest

                    // Ashhadu an la ilaha illallah
                    Triple(g4, 0.50, 0.8),
                    Triple(a4, 0.60, 0.9),
                    Triple(c5, 0.90, 1.0),
                    Triple(bb4, 0.50, 0.85),
                    Triple(a4, 0.70, 0.7),
                    Triple(g4, 1.10, 0.6),
                    Triple(0.0, 0.45, 0.0)
                )

                for ((freq, durationSec, noteVol) in melody) {
                    if (!isActive || audioTrack == null) break
                    val numSamples = (sampleRate * durationSec).toInt()
                    val buffer = ShortArray(numSamples)

                    if (freq > 0.0) {
                        val fadeLength = (sampleRate * 0.05).toInt()
                        for (i in 0 until numSamples) {
                            val time = i.toDouble() / sampleRate
                            // Add fundamental + overtone for flute/vocal resonance
                            val wave1 = sin(2.0 * Math.PI * freq * time)
                            val wave2 = 0.35 * sin(4.0 * Math.PI * freq * time)
                            val wave3 = 0.15 * sin(6.0 * Math.PI * freq * time)
                            var amplitude = (wave1 + wave2 + wave3) / 1.5

                            // Envelope attack & release to avoid click
                            val env = when {
                                i < fadeLength -> (i.toDouble() / fadeLength)
                                i > numSamples - fadeLength -> ((numSamples - i).toDouble() / fadeLength)
                                else -> 1.0
                            }

                            val sampleVal = (amplitude * env * noteVol * Short.MAX_VALUE).toInt()
                            buffer[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        }
                    } else {
                        // Silence / Rest
                        buffer.fill(0)
                    }

                    audioTrack?.write(buffer, 0, numSamples)
                }

            } catch (e: Exception) {
                Log.e(TAG, "AudioTrack playback error", e)
            } finally {
                stop()
                onCompletion?.invoke()
            }
        }
    }

    fun stop() {
        isPlaying = false
        try {
            playbackJob?.cancel()
            playbackJob = null

            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio", e)
        }
    }
}
