package com.example.gridguide

interface GuideRepository {

    /**
     * Get GuideRow items matching request filters
     *
     * @param stationId Comma-separated string of lexicographically sorted (ascending) unique station IDs.
     * @param windowStartTime Inclusive Unix time rounded down to the nearest half hour.
     * @param windowEndTime Exclusive Unix time rounded up to the nearest half hour. Note: must be in the range (windowStartTime, windowStartTime+4h].
     *
     * @return GuideRowData including guide cells
     */
    suspend fun getGuideRows(featureArea: FeatureArea, stationId: String, windowStartTime: Long, windowEndTime: Long) : ApiResult<List<GuideRowData>?>
}