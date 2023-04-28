package com.example.gridguide.mapper

import com.example.gridguide.model.ContentType
import com.example.gridguide.model.GuideCell
import com.example.gridguide.model.GuideCellContentType
import com.example.gridguide.model.GuideCellData
import com.example.gridguide.model.GuideRow
import com.example.gridguide.model.GuideRowData

object GuideMapper {

    /**
     * Map JSON response to Domain model GuideRowData
     */
    fun mapToGuideRowData(response: List<GuideRow>): List<GuideRowData> {
        val guideRowData = mutableListOf<GuideRowData>()
        response.forEach {
            guideRowData.add(
                GuideRowData(
                    stationId = it.stationId,
                    guideCells = mapToGuideCellData(it.guideCells)
                )
            )
        }
        return guideRowData
    }

    /**
     * Map JSON GuideCell response to Domain model GuideCellData
     */
    private fun mapToGuideCellData(guideCells: List<GuideCell>): List<GuideCellData> {
        val guideCellData = mutableListOf<GuideCellData>()
        guideCells.forEach {
            guideCellData.add(
                GuideCellData(
                    collectionId = it.collectionId,
                    contentId = it.contentId,
                    contentType = mapContentType(it.contentType),
                    duration = it.duration,
                    offerId = it.offerId,
                    startTime = it.startTime,
                    title = it.title,
                    isAdult = it.isAdult,
                    isNew = it.isNew,
                    isPPV = it.isPPV,
                    movieYear = it.movieYear,
                    isRecordable = it.isRecordable,
                    isStartOver = it.isStartover,
                    isCatchUp = it.isCatchup
                )
            )
        }
        return guideCellData
    }

    /**
     * Map Content Type to Domain Content Type enum class
     */
    private fun mapContentType(contentType: ContentType): GuideCellContentType {
        return when (contentType) {
            ContentType.episode -> GuideCellContentType.EPISODE
            ContentType.movie -> GuideCellContentType.MOVIE
            ContentType.series -> GuideCellContentType.SERIES
            ContentType.special -> GuideCellContentType.SPECIAL
        }
    }
}