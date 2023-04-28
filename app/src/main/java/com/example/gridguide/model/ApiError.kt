package com.example.gridguide.model

data class ApiError(
    // httpStatusCode -> Is actually the responseCode we need to set
    val httpStatusCode: Int,
    // serviceErrorCode -> Is API specific error code in enum
    val serviceErrorCode: ServiceErrorCode,
    // serviceErrorMessage -> Is based on API specific errorMessage
    val serviceErrorMessage: String,
    // requestId -> Is the API specific requestId
    val requestId: String,
    // errorType -> Based of the errorCode or serviceErrorCode [ErrorType] will change
    val errorType: ErrorType
)