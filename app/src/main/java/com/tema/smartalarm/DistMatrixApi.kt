package com.tema.smartalarm

import android.util.Log
import com.tema.smartalarm.api_services.DistanceMatrixApiService
import kotlinx.coroutines.Job
import java.util.*

class DistMatrixApi {
    companion object {
        private const val TAG = "DistMatrixApi"
    }

    suspend fun getTripDuration(departTime: String, coordsList: List<String>, mode: String): Int {
        val calendar = getTimeFromText(departTime)
        val depTime = ((calendar.timeInMillis + calendar.timeZone.rawOffset)/ 1000L).toInt()

        // take coords in format: latitude,longitude
        Log.d(TAG, "${coordsList[0]}, ${coordsList[1]}, $mode, $depTime")
        val tripDuration = getDuration(coordsList[0], coordsList[1], mode, depTime)
        var arrivalTime: String
        with(calendar) {
            add(java.util.Calendar.SECOND, tripDuration)
            arrivalTime = "${get(Calendar.HOUR_OF_DAY)}:${get(Calendar.MINUTE)}:${get(Calendar.SECOND)}"
        }
        Log.d(TAG, "Destination time: $arrivalTime")
        return tripDuration
    }

    private fun getTimeFromText(text: String): Calendar {
        val timeList = text.split(':')
        val calendar = Calendar.getInstance()
        with(calendar) {
            val time = (timeList[0] + timeList[1]).toInt()
            add(Calendar.HOUR_OF_DAY, timeZone.rawOffset / 3600000)
            val currentTime = (get(Calendar.HOUR_OF_DAY).toString() + get(Calendar.MINUTE).toString()).toInt()
            if (time < currentTime)
                add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, timeList[0].toInt())
            set(Calendar.MINUTE, timeList[1].toInt())
        }
        return calendar
    }

    private suspend fun getDuration(origins: String,
                                    destination: String,
                                    mode: String,
                                    departureTime: Int /* UNIX-format */) : Int {
        val response = DistanceMatrixApiService().getDistMatrix(
            origins,
            destination,
            mode.toLowerCase(),
            departureTime
        ).await()
        Log.d(TAG, "I'm in getDuration($origins, $destination, $mode, $departureTime)")
        return response.rows[0].elements[0].duration.seconds
    }
}