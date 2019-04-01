package net.evendanan.hamilton4erela

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class LotteryActivity : AppCompatActivity() {

    private val recorderConnection = MyServiceConnection()

    private val handler = Handler()
    private val delayedRecordStop = { unbindService(recorderConnection) }

    private val progressWaitingView by lazy { findViewById<View>(R.id.main_waiting_spinning) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lottery)
        findViewById<View>(R.id.cta_call_now).setOnClickListener { startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:${getText(R.string.box_office_phone_number)}"))) }
    }


    inner class MyServiceConnection : ServiceConnection {
        private var binder: RecorderService.LocalBinder? = null

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("HamiltonPrank", "Disconnected to $name")
            binder?.stopRecord()
            binder = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i("HamiltonPrank", "Connected to $name")
            binder = service as RecorderService.LocalBinder
            binder?.startRecord()

            //automatically stop recording after 20 seconds
            handler.postDelayed(delayedRecordStop, 20_000L)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("HamiltonPrank", "Disconnecting from recorder")

        handler.removeCallbacks(delayedRecordStop)
        unbindService(recorderConnection)
    }

    override fun onStart() {
        super.onStart()
        progressWaitingView.animate()
            .withStartAction { progressWaitingView.visibility = View.VISIBLE }
            .setStartDelay(1500)
            .setDuration(66)
            .alpha(1.0f)
            .withEndAction(::hideProgress)
            .start()
    }

    private fun hideProgress() {
        progressWaitingView.animate()
            .setStartDelay(3000)
            .setDuration(250)
            .alpha(0.0f)
            .withEndAction(::startShowingWeWonCard)
            .start()
    }

    private fun startShowingWeWonCard() {
        progressWaitingView.visibility = View.GONE
        findViewById<View>(R.id.root_ui).apply {
            animate()
                .withStartAction { this.visibility = View.VISIBLE }
                .setDuration(100)
                .alpha(1f)
                .start()
        }
    }
}
