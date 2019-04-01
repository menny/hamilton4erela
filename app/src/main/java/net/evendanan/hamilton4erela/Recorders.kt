package net.evendanan.hamilton4erela

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File


interface Recorder {
    fun startRecording()
    fun stopRecording()
}

//class ImagesRecorder(private val context: Context, private val outputFolder: File) : Recorder {
//    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//
//    override fun startRecording() {
//        cameraManager.cameraIdList
//            .map { it to cameraManager.getCameraCharacteristics(it) }
//            .first { it.second.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT }
//            .let { it.first }
//            .let {
//                Log.i("Erela ImagesRecorder", "Asking to open camera $it")
//                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                    cameraManager.openCamera(it, CameraDeviceStateCallback(), null)
//                } else {
//                    Log.i("Erela ImagesRecorder", "No camera permission for $it")
//                }
//            }
//    }
//
//    private inner class CameraDeviceStateCallback : CameraDevice.StateCallback() {
//        override fun onOpened(camera: CameraDevice) {
//            Log.i("Erela ImagesRecorder", "CameraDeviceStateCallback onOpened ${camera.id}")
//            camera.createCaptureSession(emptyList(), object : CameraCaptureSession.StateCallback() {
//                override fun onConfigureFailed(session: CameraCaptureSession) {
//                    Log.i("Erela VideoRecorder", "Failed to start a capture session")
//                }
//
//                override fun onConfigured(session: CameraCaptureSession) {
//                    session.
//                }
//            }, null)
//        }
//
//        override fun onDisconnected(camera: CameraDevice) {
//            Log.i("Erela VideoRecorder", "CameraDeviceStateCallback onDisconnected ${camera.id}")
//            stopRecording()
//        }
//
//        override fun onError(camera: CameraDevice, error: Int) {
//            Log.i("Erela VideoRecorder", "CameraDeviceStateCallback onError ${camera.id} error code $error")
//            stopRecording()
//        }
//    }
//
//    override fun stopRecording() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//}

class VideoRecorder(private val context: Context, private val outputFile: File) : Recorder {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val recorder = MediaRecorder()


    override fun startRecording() {
        recorder.apply {
            Log.i("Erela VideoRecorder", "Will record to $outputFile")

            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            setVideoEncodingBitRate(1600 * 1000)
//            setVideoFrameRate(30)
//            setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT)
//            setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            setOrientationHint(0)
//
//            prepare()
        }

        startRecordWithCameraCapture()
    }

    override fun stopRecording() {
        Log.i("Erela VideoRecorder", "Stopping recording")
        recorder.apply {
            stop()
            reset()
            release()
        }
    }

    private fun startRecordWithCameraCapture() {
        Log.i("Erela VideoRecorder", "Starting recording")

        var cameraIdIndex = 0
        cameraManager.cameraIdList
            .map { Triple(it, cameraManager.getCameraCharacteristics(it), cameraIdIndex++) }
            .first { (_, cameraCharacteristics, _) -> cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT }
            .let { (cameraId, _, cameraIndex) ->
                Log.i("Erela VideoRecorder", "Asking to open camera $cameraId, index $cameraIndex")
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(cameraId, CameraDeviceStateCallback(cameraIndex), null)
                } else {
                    Log.i("Erela VideoRecorder", "No camera permission for $cameraId, index $cameraIndex")
                }
            }
    }

    private fun startRecordAfterCameraCapture(camera: CameraDevice, cameraIndex: Int) {
        //preparing recorder for high-quality
        recorder.setProfile(CamcorderProfile.get(cameraIndex, CamcorderProfile.QUALITY_HIGH))
        recorder.setOutputFile(outputFile.absolutePath)
        recorder.prepare()

        camera.createCaptureSession(listOf(recorder.surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.i("Erela VideoRecorder", "Failed to start a capture session")
            }

            override fun onConfigured(session: CameraCaptureSession) {
                recorder.start()
            }
        }, null)
    }

    private inner class CameraDeviceStateCallback(private val cameraIndex: Int) : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.i("Erela VideoRecorder", "CameraDeviceStateCallback onOpened ${camera.id}")
            startRecordAfterCameraCapture(camera, cameraIndex)
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.i("Erela VideoRecorder", "CameraDeviceStateCallback onDisconnected ${camera.id}")
            stopRecording()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.i("Erela VideoRecorder", "CameraDeviceStateCallback onError ${camera.id} error code $error")
            stopRecording()
        }
    }
}
