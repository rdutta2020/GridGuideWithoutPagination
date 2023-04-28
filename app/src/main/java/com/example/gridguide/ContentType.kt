package com.example.gridguide

import com.squareup.moshi.Json

enum class ContentType(val value: String) {

    @Json(name = "episode")
    episode("episode"),

    @Json(name = "movie")
    movie("movie"),

    @Json(name = "series")
    series("series"),

    @Json(name = "special")
    special("special");

    /**
     * Override toString() to avoid using the enum variable name as the value, and instead use
     * the actual value defined in the API spec file.
     *
     * This solves a problem when the variable name and its value are different, and ensures that
     * the client sends the correct enum values to the server always.
     */
    override fun toString(): String = value

    companion object {
        /**
         * Converts the provided [data] to a [String] on success, null otherwise.
         */
        fun encode(data: kotlin.Any?): String? = if (data is ContentType) "$data" else null

        /**
         * Returns a valid [ContentType] for [data], null otherwise.
         */
        fun decode(data: kotlin.Any?): ContentType? = data?.let {
            val normalizedData = "$it".lowercase()
            values().firstOrNull { value ->
                it == value || normalizedData == "$value".lowercase()
            }
        }
    }
}
