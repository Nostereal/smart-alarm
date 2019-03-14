package com.tema.smartalarm.api_services

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.tema.smartalarm.model.DistMatrix
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY = "807c3eb4-8204-48c9-b35f-0a0d0b6a6c92"

interface DistanceMatrixApiService {

    @GET("/v1.0.0/distancematrix?")
    fun getDistMatrix(@Query("origins") origins: String,
                      @Query("destinations") destinations: String,
                      @Query("mode") mode: String,
                      @Query("departure_time") departureTime: Int) : Deferred<DistMatrix>

    companion object {
        operator fun invoke(): DistanceMatrixApiService {
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
                .baseUrl("https://api.routing.yandex.net")
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DistanceMatrixApiService::class.java)
        }
    }
}