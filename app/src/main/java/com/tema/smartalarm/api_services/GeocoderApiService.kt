package com.tema.smartalarm.api_services

import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.tema.smartalarm.model.Geocoder
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY = "3df28ee6-3512-4c1c-9837-ff76310df30c"

interface GeocoderApiService {

    @GET("/1.x/?")
    fun getCoords(@Query("geocode") geocode: String,
                  @Query("format") format: String) : Deferred<Geocoder>

    companion object {
        operator fun invoke(): GeocoderApiService {
            val requestInterceptor = Interceptor { chain ->
                val url = chain.request()
                    .url()
                    .newBuilder()
                    .addQueryParameter("apikey", API_KEY)
                    .build()

                val request = chain.request()
                    .newBuilder()
                    .url(url)
                    .build()

                return@Interceptor chain.proceed(request)
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(requestInterceptor)
                .build()

            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://geocode-maps.yandex.ru")
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeocoderApiService::class.java)
        }
    }

}