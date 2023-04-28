package com.example.gridguide

import com.squareup.moshi.Json

data class GuideRow (

    /* Station ID for a given guide row. */
    @Json(name = "stationId")
    val stationId: kotlin.String,

    /* Array of guide cells for a given guide row. */
    @Json(name = "guideCells")
    val guideCells: List<GuideCell>

)

