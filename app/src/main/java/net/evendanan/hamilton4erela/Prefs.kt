package net.evendanan.hamilton4erela

import android.content.Context
import android.preference.PreferenceManager

class Prefs(context: Context) {
    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun setPrankTime(hour: Int, minute: Int) {
        sharedPrefs.edit()
            .putInt(PRANK_HOUR, hour)
            .putInt(PRANK_MINUTE, minute)
            .apply()
    }

    fun getPrankTime() = Pair(sharedPrefs.getInt(PRANK_HOUR, 12), sharedPrefs.getInt(PRANK_MINUTE, 1))

    companion object {
        private const val PRANK_HOUR = "PRANK_HOUR"
        private const val PRANK_MINUTE = "PRANK_MINUTE"
    }
}
