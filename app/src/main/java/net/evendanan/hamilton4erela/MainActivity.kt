package net.evendanan.hamilton4erela

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

private const val REQUEST_RECORD_PERMISSION = 200

class MainActivity : AppCompatActivity() {

    private val prefs by lazy { Prefs(applicationContext) }
    private val alarmPendingIntent by lazy {
        PendingIntent.getBroadcast(
            applicationContext,
            ShowPrankNotificationReceiver.REQUEST_CODE,
            Intent(applicationContext, ShowPrankNotificationReceiver::class.java),
            0
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE),
            REQUEST_RECORD_PERMISSION
        )
    }

    override fun onStart() {
        super.onStart()
        findViewById<TimePicker>(R.id.prank_time).apply {
            prefs.getPrankTime().let {
                hour = it.first
                minute = it.second
            }

            setOnTimeChangedListener { _, hourOfDay, minuteOfHour -> prefs.setPrankTime(hourOfDay, minuteOfHour) }
        }

        findViewById<View>(R.id.set_prank_time).setOnClickListener { setupAlarm() }
        findViewById<View>(R.id.clear_prank_time).setOnClickListener {
            (getSystemService(Context.ALARM_SERVICE) as AlarmManager).apply {
                cancel(alarmPendingIntent)
            }
        }

        findViewById<Switch>(R.id.show_settings_app_icon).let { appIconSwitch ->
            val lotteryLauncherActivityComponent = ComponentName(applicationContext, MainActivity::class.java)
            packageManager.apply {
                appIconSwitch.isChecked = when (getComponentEnabledSetting(lotteryLauncherActivityComponent)) {
                    COMPONENT_ENABLED_STATE_DEFAULT, COMPONENT_ENABLED_STATE_ENABLED -> true
                    else -> false
                }

                appIconSwitch.setOnCheckedChangeListener { _, isChecked ->
                    setComponentEnabledSetting(lotteryLauncherActivityComponent, if (isChecked) COMPONENT_ENABLED_STATE_ENABLED else COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
                }
            }
        }

        findViewById<View>(R.id.launch_lottery_activity).setOnClickListener { startActivity(Intent(this@MainActivity, LotteryActivity::class.java)) }
    }

    internal fun setupAlarm() {
        val (hour, minute) = prefs.getPrankTime()
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).apply {
            cancel(alarmPendingIntent)

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmPendingIntent)
        }
    }
}
