package com.example.gridguide

import android.content.Context
import com.example.gridguide.GuideMapper.mapToGuideRowData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GuideRepositoryImpl @Inject constructor(
    private val guideApi: GuideApi,
    private val msoPartnerId: String?,
    private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
) : BaseRepository(ioDispatcher), GuideRepository {

    override suspend fun getGuideRows(
        featureArea: FeatureArea,
        stationId: String,
        windowStartTime: Long,
        windowEndTime: Long
    ): ApiResult<List<GuideRowData>?> {
        if (msoPartnerId != null) {
            val resource =
                invokeApiRequest {
                    guideApi.guideRowsGet(
                        featureArea = featureArea.format(context),
                        msoPartnerId = msoPartnerId,
                        stationId = stationId,
                        windowStartTime = windowStartTime,
                        windowEndTime = windowEndTime
                    )
                }
            return withContext(ioDispatcher + exceptionHandler) {
                resource.map(resource.data?.let { mapToGuideRowData(it) })
            }
        } else {
            return withContext(ioDispatcher + exceptionHandler) {
                ApiResult.Failure(clientApiError("msoPartnerId is null"))
            }
        }
    }

    companion object {
        private const val TAG = "GuideRepository"
    }
}
