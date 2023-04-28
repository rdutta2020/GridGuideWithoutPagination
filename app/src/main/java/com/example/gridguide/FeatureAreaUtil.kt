package com.example.gridguide

import android.content.Context

fun FeatureArea.format(context: Context): String {
    return when (screens.size) {
        0 -> ""
        1 -> format(context, screens.first())
        else -> context.getString(
            R.string.feature_area_header_format,
            format(context, screens.first()),
            format(context, screens.last())
        )
    }
}

private fun format(context: Context, screen: Screen) : String {
    return when(screen.type) {
        Screen.Type.APP_STARTUP -> context.getString(R.string.FEATURE_AREA_APP_STARTUP)
        Screen.Type.GUIDE -> context.getString(R.string.FEATURE_AREA_GUIDE)
    }
}

/**
 * Application feature area header.
 */
const val HEADER_FEATURE_AREA = "ApplicationFeatureArea"