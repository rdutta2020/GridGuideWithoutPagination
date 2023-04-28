package com.example.gridguide

import java.io.Serializable

data class FeatureArea(
    /**
     * Screens defining current application context.
     */
    val screens: List<Screen>
)  : Serializable {
    /**
     * Constructs an empty [FeatureArea].
     */
    constructor() : this(emptyList())

    /**
     * Constructs a [FeatureArea] consisting of a single [Screen].
     */
    constructor(screen: Screen) : this(listOf(screen))

    /**
     * Constructs a [FeatureArea] consisting of screens of [previousFeatureArea] and the given [screen].
     */
    constructor(previousFeatureArea: FeatureArea?, screen: Screen) : this(
        (previousFeatureArea ?: FeatureArea()).screens + screen
    )
}
