package com.example.gridguide.model

sealed class ApiResult<out T>(
    val data: T? = null,
    val error: ApiError? = null
) {
    /**
     * This is the [Success] block
     * @param data -> Contain the data object
     */
    class Success<out T>(data: T) :
        ApiResult<T>(data)

    /**
     * This is the [Success] block
     * @param data ->  Contain the data object there by default set to null
     * @param error -> Contain the [ApiError] object
     */
    class Failure<out T>(error: ApiError?, data: T? = null) :
        ApiResult<T>(data, error)

    /**
     * This is the [NoNetwork] block
     * @param data -> Contain the data object there by default set to null
     * @param error -> Contain the [ApiError] object
     */
    class NoNetwork<out T>(error: ApiError?, data: T? = null) :
        ApiResult<T>(data, error)

    /**
     * This function will help the map all types of data
     */
    fun <R> map(data: R?) =
        when (this) {
            // In the case of Success
            is Success -> Success(data)
            // In the case of Failure
            is Failure -> Failure(this.error, data)
            // In the case of NoNetwork
            is NoNetwork -> NoNetwork(this.error, data)
        }
}