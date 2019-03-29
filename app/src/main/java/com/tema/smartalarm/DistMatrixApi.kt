package com.tema.smartalarm

import android.util.Log
import com.tema.smartalarm.api_services.DistanceMatrixApiService
import com.tema.smartalarm.utils.getTimeFromText
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