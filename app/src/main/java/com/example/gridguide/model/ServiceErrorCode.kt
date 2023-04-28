package com.example.gridguide.model

enum class ServiceErrorCode {
    // Api Gate way error
    APIGW_ERROR,

    // Bad Request Exception
    BAD_REQUEST,

    // Internal server error
    INTERNAL_SERVER_ERROR,

    // Unexpected error from server type
    unexpectedError,

    // Dynamo Db Error
    dynamoDbError,

    // API gateway 401 error code
    APIGW_401,

    // API gateway 4XX error code
    APIGW_4XX,

    // API gateway 5XX error code
    APIGW_5XX,

    // Object not found error
    objectNotFound,

    // Resource not found
    RESOURCE_NOT_FOUND,

    // Client error
    GENERIC
}