package com.example.gridguide

import com.squareup.moshi.Json

data class GuideCell (

    /* Collection ID for a given guide cell. */
    @Json(name = "collectionId")
    val collectionId: kotlin.String,

    /* Content ID for a given guide cell. */
    @Json(name = "contentId")
    val contentId: kotlin.String,

    @Json(name = "contentType")
    val contentType: ContentType,

    /* Showing duration in seconds. */
    @Json(name = "duration")
    val duration: kotlin.Int,

    /* Offer ID for a given guide cell. */
    @Json(name = "offerId")
    val offerId: kotlin.String,

    /* Showing start time as Unix time. */
    @Json(name = "startTime")
    val startTime: kotlin.Long,

    /* Showing title. */
    @Json(name = "title")
    val title: kotlin.String,

    /* When true, showing should be treated as adult. */
    @Json(name = "isAdult")
    val isAdult: kotlin.Boolean? = false,

    /* When true, showing should be treated as new. */
    @Json(name = "isNew")
    val isNew: kotlin.Boolean? = false,

    /* When true, showing supports PPV. */
    @Json(name = "isPPV")
    val isPPV: kotlin.Boolean? = false,

    /* The year the movie was released. */
    @Json(name = "movieYear")
    val movieYear: kotlin.Int? = null,

    /* When true, showing can be recorded. */
    @Json(name = "isRecordable")
    val isRecordable: kotlin.Boolean? = true,

    /* When true, showing supports start-over. */
    @Json(name = "isStartover")
    val isStartover: kotlin.Boolean? = false,

    /* When true, showing supports catch-up. */
    @Json(name = "isCatchup")
    val isCatchup: kotlin.Boolean? = false

)

