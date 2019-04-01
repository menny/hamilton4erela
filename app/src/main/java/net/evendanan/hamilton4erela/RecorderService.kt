package net.evendanan.hamilton4erela

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Environment
import java.io.File


class RecorderService : Service() {
    private lateinit var recorder: Recorder

    override fun onBind(intent: Intent) = LocalBinder()

    inner class LocalBinder : Binder() {
        fun startRecord() {
            recorder.startRecording()
        }

        fun stopRecord() {
            recorder.stopRecording()
        }
    }

    override fun onCreate() {
        super.onCreate()

        recorder = VideoRecorder(this, File(Environment.getExternalStorageDirectory().absoluteFile, "hamilton_lottery_video.mp4"))
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder.stopRecording()
    }
}
