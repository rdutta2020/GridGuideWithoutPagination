package com.example.gridguide

data class Screen(
    /**
     * Screen type.
     */
    val type: Type,
    /**
     * Description of screen content.
     */
    val contentDescription: String? = null
) {

    /**
     * Screen type enum.
     */
    enum class Type {
        /**
         * Application Startup.
         */
        APP_STARTUP,

        /**
         * Guide
         */
        GUIDE
    }

}