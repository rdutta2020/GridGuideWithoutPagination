package com.example.gridguide.model

enum class ErrorType {
    // Set SERVICE ERROR for any 5XX errorCode
    SERVICE_ERROR,

    // Set CLIENT ERROR for any 4XX errorCode
    CLIENT_ERROR,

    // Set NETWORK ERROR for any IO exception
    NETWORK_ERROR,

    // Set UNKNOWN ERROR for any type of unKnown errorCode
    UNKNOWN_ERROR
}