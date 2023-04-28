package com.example.gridguide

data class GuideCellData(

    /* Collection ID for a given guide cell. */
    val collectionId: String,

    /* Content ID for a given guide cell. */
    val contentId: String,

    /* Content Type for a given guide cell. */
    val contentType: GuideCellContentType,

    /* Showing duration in seconds. */
    val duration: Int,

    /* Offer ID for a given guide cell. */
    val offerId: String,

    /* Showing start time as Unix time. */
    val startTime: Long,

    /* Showing title. */
    val title: String,

    /* When true, showing should be treated as adult. */
    val isAdult: Boolean? = false,

    /* When true, showing should be treated as new. */
    val isNew: Boolean? = false,

    /* When true, showing supports PPV. */
    val isPPV: Boolean? = false,

    /* The year the movie was released. */
    val movieYear: Int? = null,

    /* When true, showing can be recorded. */
    val isRecordable: Boolean? = true,

    /* When true, showing supports start-over. */
    val isStartOver: Boolean? = false,

    /* When true, showing supports catch-up. */
    val isCatchUp: Boolean? = false
)
