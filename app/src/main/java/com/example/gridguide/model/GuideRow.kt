package com.example.gridguide.model

import com.example.gridguide.model.GuideCell
import com.squareup.moshi.Json

data class GuideRow (

    /* Station ID for a given guide row. */
    @Json(name = "stationId")
    val stationId: kotlin.String,

    /* Array of guide cells for a given guide row. */
    @Json(name = "guideCells")
    val guideCells: List<GuideCell>

)

