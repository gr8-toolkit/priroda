package com.parimatch.navigation.limitation

import com.parimatch.navigation.receiver.popTab
import com.parimatch.navigation.registry.ScreensRegistry
import com.parimatch.navigation.screen.Screen
import com.parimatch.navigation.state.Action
import com.parimatch.navigation.state.firstScreenInState

internal fun ScreensRegistry.popDownTo(screen: Screen<out Any>) =
	Action { state ->
		if (!state.currentTabScreens.contains(screen)) return@Action state
		if (state.currentTabScreens.firstScreenInState().uid == screen.uid){
			popTab(state.currentTab).act(state)
		} else {
			state.copyOnCurrentScreens { screens ->
				screens.take(screens.indexOf(screen))
			}.run {
			    screen.afterCloseTarget
                    ?.let { intent -> resolverScreenByTarget(target = intent, withAnswerTarget = false) }
                    ?.let { after -> copyOnCurrentScreens { it + after } }
                    ?: this
            }
		}
	}
