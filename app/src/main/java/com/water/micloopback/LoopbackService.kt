package com.water.micloopback

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.*
import android.content.pm.PackageManager
import android.media.*
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class LoopbackService : Service() {

    private var isLooping = false
    private lateinit var audioRecord: AudioRecord
    private lateinit var audioTrack: AudioTrack
    private lateinit var audioThread: Thread

    companion object {
        const val CHANNEL_ID = "LoopbackChannel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()


        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mic Loopback 正在运行")
            .setContentText("点击进入应用")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()

        startForeground(NOTIFICATION_ID, notification)
        startLoopback()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoopback()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "MicLoopback 前台服务",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun startLoopback() {
        // ⚠️ format似乎与麦克风配置不匹配仍然能work
        val sampleRate = 44100
        val channelInConfig = AudioFormat.CHANNEL_IN_MONO
        val channelOutConfig = AudioFormat.CHANNEL_OUT_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelInConfig, audioFormat) * 2

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            return
        } else {
            // 初始化 AudioRecord（录音器）
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelInConfig,
                audioFormat,
                bufferSize
            )
        }

        // 初始化 AudioTrack（扬声器播放）
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            channelOutConfig,
            audioFormat,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        // 启动录音和播放
        audioRecord.startRecording()
        audioTrack.play()
        isLooping = true

        // 后台线程处理音频流转发
        audioThread = Thread {
            val buffer = ByteArray(bufferSize)
            while (isLooping) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {
                    audioTrack.write(buffer, 0, read)
                }
            }
        }
        audioThread.start()
    }

    private fun stopLoopback() {
        isLooping = false
        audioThread.join()

        audioRecord.stop()
        audioRecord.release()

        audioTrack.stop()
        audioTrack.release()
    }
}