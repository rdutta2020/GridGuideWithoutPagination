package com.example.gridguide.model

import com.example.gridguide.model.GuideCellData

data class GuideRowData (

    /* Station ID for a given guide row. */
    val stationId: String,

    /* List of guide cells for a given guide row. */
    val guideCells: List<GuideCellData>
)