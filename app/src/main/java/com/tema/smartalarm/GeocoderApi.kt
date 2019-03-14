package com.tema.smartalarm

import android.util.Log
import com.tema.smartalarm.api_services.GeocoderApiService
import kotlinx.coroutines.*

class GeocoderApi {
    companion object {
        private const val TAG = "GeocoderApi"
    }
    private lateinit var job: Job

    suspend fun getCoordsList(departAddress: String, destAddress: String): List<String> {
        val coordsList = mutableListOf<String>()
        // TODO: what can i do instead of runBlocking?
        job = GlobalScope.launch(Dispatchers.IO) {
            // get coords in format: latitude longitude
            val originsDef = async { getCoords(departAddress) }
            val destCoordsDef = async { getCoords(destAddress) }

            val origins = originsDef.await()
            val destCoords = destCoordsDef.await()
            Log.d(TAG, "$origins, $destCoords")
            coordsList.add(origins)
            coordsList.add(destCoords)
        }
        job.join()
        Log.d(TAG, "after launch: $coordsList")
        return coordsList
    }

    private suspend fun getCoords(address: String) : String {
        Log.d(TAG, "Address: $address")
        val response = GeocoderApiService().getCoords(
            address,
            "json"
        ).await()

        var coords = response.response.GeoObjectCollection.featureMember[0].GeoObject.Point.pos
        coords = reverseCoords(coords)
        Log.d(TAG, "I'm in getCoords($address)")
        Log.d(TAG, "coords = $coords")
        return coords
    }

    private fun reverseCoords(coords: String): String {
        val coordsList = coords.split(' ')
        return "${coordsList[1]},${coordsList[0]}"
    }
}