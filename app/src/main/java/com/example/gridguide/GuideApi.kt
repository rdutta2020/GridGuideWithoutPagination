package com.example.gridguide

import com.skydoves.sandwich.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GuideApi {

    @GET("v1/guideRows")
    suspend fun guideRowsGet(@Header("ApplicationFeatureArea") featureArea: String, @Query("msoPartnerId") msoPartnerId: String, @Query("stationId") stationId: String, @Query("windowStartTime") windowStartTime: Long, @Query("windowEndTime") windowEndTime: Long): ApiResponse<List<GuideRow>>
}
