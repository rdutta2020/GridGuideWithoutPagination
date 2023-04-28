package com.example.gridguide

data class GuideRowData (

    /* Station ID for a given guide row. */
    val stationId: String,

    /* List of guide cells for a given guide row. */
    val guideCells: List<GuideCellData>
)