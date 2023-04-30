package com.example.gridguide.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.gridguide.model.GuideRow
import com.example.gridguide.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GuideViewModel : ViewModel() {

    val msoPartnerId = "tivo:pt.5058"
    val featureAreaName = "GridGuide"
    val stationId = "tivo:st.10420224"

    fun getGridGuide() {
        val call = RetrofitInstance.api.guideRowsGet(
            featureArea = featureAreaName,
            msoPartnerId = msoPartnerId,
            stationId = stationId,
            windowStartTime = 1681984800,
            windowEndTime = 1681986600,
            applicationVersion = "4.11.0-20230314-0430",
            deviceType = "androidPhone",
            requestId = 1234,
            language = "en-US",
            productName = "Tivo Mobile IPTV v.4.11.0-20230314-0430 Quickdroid",
            amznRequestId = "1678820014055-3047906886224678661",
            applicationName = "ApplicationName: 4.9",
            bodyId = "tsn:A8F0F000021749C",
            encoding = "gzip",
            userAgent = "vscode-restclient"
        )

        call.enqueue(object : Callback<List<GuideRow>> {
            override fun onResponse(
                call: Call<List<GuideRow>>,
                response: Response<List<GuideRow>>
            ) {
                Log.d("SHYAKDAS", "APIRESPONSE==${response.body()}")
            }

            override fun onFailure(call: Call<List<GuideRow>>, t: Throwable) {
                // Handle error response
                Log.d("SHYAKDAS", "APIRESPONSE==${t.message}")
            }
        })
    }
}