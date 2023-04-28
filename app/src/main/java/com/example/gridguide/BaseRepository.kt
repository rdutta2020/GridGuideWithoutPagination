package com.example.gridguide

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.getOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

open class BaseRepository(private val dispatcher: CoroutineDispatcher) {

    /**
     * [exceptionHandler] will use to log any type of throwable object
     */
    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    /**
     * [invokeApiRequest] is the function which need to call from all the child [BaseRepository],
     * this function is responsible to handle Success and Failure state
     * @param call -> Unit function
     */
    suspend fun <T> invokeApiRequest(call: suspend () -> ApiResponse<T>): ApiResult<T?> =
        withContext(dispatcher + exceptionHandler) {
            return@withContext try {
                call.invoke().let {
                    // Check if the response is SuccessFull or not
                    when (it) {
                        is ApiResponse.Success -> {
                            ApiResult.Success(data = it.data)
                        }

                        is ApiResponse.Failure.Error -> {
                            ApiResult.Failure(
                                ApiError(
                                    500,
                                    ServiceErrorCode.APIGW_ERROR,
                                    "",
                                    "",
                                    ErrorType.CLIENT_ERROR
                                ), null
                            )
                        }

                        else -> {
                            ApiResult.Failure(
                                ApiError(
                                    500,
                                    ServiceErrorCode.APIGW_ERROR,
                                    "",
                                    "",
                                    ErrorType.CLIENT_ERROR
                                ), null
                            )
                        }
                    }
                }
            } catch (t: Throwable) {
                // Set to throwable
                // val apiError = errorParser.onApiCallException(t)
                // If the response code is 100, Set to noNetwork connection else Failure
                ApiResult.Failure(
                    ApiError(
                        500,
                        ServiceErrorCode.APIGW_ERROR,
                        "",
                        "",
                        ErrorType.CLIENT_ERROR
                    ), null
                )
            }
        }

    /**
     * This is generic client API error, which will return ApiError object
     * @param errorMessage -> Pass the error message to get into ApiError
     */
    fun clientApiError(errorMessage: String): ApiError {
        return ApiError(
            httpStatusCode = 0,
            serviceErrorCode = ServiceErrorCode.GENERIC,
            serviceErrorMessage = errorMessage,
            requestId = "",
            errorType = ErrorType.CLIENT_ERROR
        )
    }
}
