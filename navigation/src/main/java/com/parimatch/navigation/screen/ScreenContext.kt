package com.parimatch.navigation.screen

import java.io.Serializable

public data class ScreenContext(
    val place: Place,
    val animation: Animation?
) : Serializable {

	public companion object {
		public val DEFAULT: ScreenContext = ScreenContext(Place.DEFAULT, null)
		public val DEFAULT_FULLSCREEN: ScreenContext = ScreenContext(Place.FULLSCREEN, null)
	}
}

public enum class Animation {
	CROSSFADE
}

public enum class Place {
	DEFAULT,
	FULLSCREEN
}
