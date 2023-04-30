package com.example.gridguide.network

import com.example.gridguide.api.GuideApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: GuideApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api-cloudcore-guide-service-staging.prod.tivoservice.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GuideApi::class.java)
    }
}