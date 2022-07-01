package com.parimatch.navigation.state

import com.parimatch.navigation.screen.Screen
import java.io.Serializable
import java.lang.IllegalArgumentException

internal fun List<Screen<out Any>>.firstScreenInState() = firstOrNull() ?: throw IllegalArgumentException(State.CONTRACT_EXCEPTION_MESSAGE_2)

internal fun List<Screen<out Any>>.lastScreenInState() = lastOrNull() ?: throw IllegalArgumentException(State.CONTRACT_EXCEPTION_MESSAGE_2)

internal data class State(
	val backStackTabs: List<Tab>,
	val screens: Map<Tab, List<Screen<out Any>>>
) : Serializable {

    companion object {
        internal const val CONTRACT_EXCEPTION_MESSAGE_1 = "Tabs cannot be empty"
        internal const val CONTRACT_EXCEPTION_MESSAGE_2 = "List of Screens cannot be empty"
        internal const val CONTRACT_EXCEPTION_MESSAGE_3 = "Screens must contain entry with current tab"
    }

	internal val currentTab: Tab get() = backStackTabs.lastOrNull()!! // due to init check

	internal val currentTabScreens: List<Screen<out Any>>
		get() = screens[currentTab]!! // due to init check

	init {
		require(backStackTabs.isNotEmpty()) { CONTRACT_EXCEPTION_MESSAGE_1 }
		require(!screens.containsValue(emptyList())) { CONTRACT_EXCEPTION_MESSAGE_2 }
		require(screens[currentTab] != null) { CONTRACT_EXCEPTION_MESSAGE_3 }
	}

	internal inline fun copyOnCurrentScreens(transform: (List<Screen<out Any>>) -> List<Screen<out Any>>): State =
		copy(screens = screens.plus(currentTab to transform(currentTabScreens)))
}
