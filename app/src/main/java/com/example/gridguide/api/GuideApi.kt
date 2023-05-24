package com.example.gridguide.api

import com.example.gridguide.model.GuideRow
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GuideApi {

    @GET("v1/guideRows")
    fun guideRowsGet(
      //  @Header("ApplicationFeatureArea") featureArea: String,
     //   @Header("ApplicationVersion") applicationVersion: String,
      //  @Header("DeviceType") deviceType: String,
        @Header("Origin-RequestId") requestId: String,
      //  @Header("Accept-Language") language: String,
     //   @Header("ProductName") productName: String,
     //   @Header("x-amzn-requestid") amznRequestId: String,
        @Header("ApplicationName") applicationName: String,
        @Header("BodyId") bodyId : String,
       // @Header("Accept-Encoding") encoding :String,
      //  @Header("user-agent")userAgent :String,
        @Query("msoPartnerId") msoPartnerId: String,
        @Query("stationId") stationId: String,
        @Query("windowStartTime") windowStartTime: Long,
        @Query("windowEndTime") windowEndTime: Long
    ): Call<List<GuideRow>>
}
