package com.tema.smartalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.getSystemService
import android.util.Log
import com.tema.smartalarm.utils.getTimeFromText
import kotlinx.coroutines.*
import java.util.*

class Receiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "Receiver"
    }
    private var job: Job? = null
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onReceive(context: Context?, intent: Intent?) {
        var departAddress = ""
        var destAddress = ""
        var departTime = ""
        var mode = ""
        var alarmTime = ""

        if (intent != null) {
            val extras = intent.extras
            departAddress = extras.getString("departAddress") ?: ""
            destAddress = extras.getString("destAddress") ?: ""
            departTime = extras.getString("departTime") ?: ""
            mode = extras.getString("mode") ?: ""
            alarmTime = extras.getString("alarmTime") ?: ""
        }
        else { Log.d(TAG, "Intent is null") }

        var destinationTime = 0L

        job = uiScope.launch {
            val coordsListDef = async(Dispatchers.IO) { GeocoderApi().getCoordsList(departAddress, destAddress) }
            val coordsList = coordsListDef.await()

            val tripDurationDef = async(Dispatchers.IO) { DistMatrixApi().getTripDuration(departTime, coordsList, mode) }
            val tripDuration = tripDurationDef.await()
            Log.d(TAG, "$tripDuration")
            destinationTime = getTimeFromText(departTime).timeInMillis + (tripDuration * 1.1).toInt()
        }

        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.HOUR) > 8)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR, 8)
        calendar.set(Calendar.MINUTE, 55)
        calendar.set(Calendar.SECOND, 0)

        if (destinationTime > calendar.timeInMillis) {
            //TODO: set alarm before basic alarm
            val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val broadcastIntent = Intent(context, AlarmReceiver::class.java)
            val pIntent = PendingIntent.getBroadcast(context, 1, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmTimeInMillis = getTimeFromText(alarmTime).timeInMillis - (destinationTime - calendar.timeInMillis)

            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                alarmTimeInMillis,
                pIntent
            )
        }
    }
}