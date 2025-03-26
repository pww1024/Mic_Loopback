package com.water.micloopback

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.water.micloopback.ui.theme.MicLoopbackTheme
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.media.*

/*
AppCompatActivity

ComponentActivity

 */



class MainActivity : ComponentActivity() {

    private var isLooping = false
    private lateinit var audioRecord: AudioRecord
    private lateinit var audioTrack: AudioTrack
    private lateinit var audioThread: Thread
    private lateinit var loopButton: Button
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loopButton = Button(this).apply {
            text = "begin Loopback"
            setOnClickListener {
                toggleLoopback()
            }
        }
        setContentView(loopButton)

        requestPermission()
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
    }

    private fun startLoopback() {
        // ⚠️ format似乎与麦克风配置不匹配仍然能work
        val sampleRate = 44100  // 常用采样率
        val channelInConfig = AudioFormat.CHANNEL_IN_MONO
        val channelOutConfig = AudioFormat.CHANNEL_OUT_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT

        // 计算缓冲区大小
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelInConfig, audioFormat)
        val bufferSize = minBufferSize * 2

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
//            // 如果未授权，请求权限
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.RECORD_AUDIO),
//                REQUEST_RECORD_AUDIO_PERMISSION
//            )
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
                val readSize = audioRecord.read(buffer, 0, buffer.size)
                if (readSize > 0) {
                    audioTrack.write(buffer, 0, readSize)
                }
            }
        }
        audioThread.start()
    }

    private fun toggleLoopback() {
        if (isLooping) {
            stopLoopback()
            loopButton.text = "begin Loopback"
        } else {
            startLoopback()
            loopButton.text = "stop Loopback"
        }
    }

    private fun stopLoopback() {
        isLooping = false
        audioThread.join()

        audioRecord.stop()
        audioRecord.release()

        audioTrack.stop()
        audioTrack.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoopback()
    }
}
