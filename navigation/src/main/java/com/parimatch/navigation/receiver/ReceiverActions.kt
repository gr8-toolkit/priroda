package com.parimatch.navigation.receiver

import com.parimatch.navigation.registry.ScreensRegistry
import com.parimatch.navigation.screen.ScreenContract
import com.parimatch.navigation.state.Action
import com.parimatch.navigation.state.Tab
import com.parimatch.navigation.state.firstScreenInState
import com.parimatch.navigation.state.lastScreenInState

internal fun <A : Any, I : Any, C : ScreenContract<A, I>> ScreensRegistry.replaceRootTab(
	tab: Tab,
	target: Target<A, I, C>
) = Action { state ->
	resolverScreenByTarget(target)?.let { screen ->
		state.copy(
			backStackTabs = listOf(tab),
			screens = mutableMapOf(tab to listOf(screen))
		)
	} ?: state
}

internal fun <A : Any, I : Any, C : ScreenContract<A, I>> ScreensRegistry.pushTab(
	tab: Tab,
	target: Target<A, I, C>
) = Action { state ->
	when {
		state.currentTab == tab -> {
			state.copyOnCurrentScreens { listOf(it.firstScreenInState()) }
		}
		state.backStackTabs.contains(tab) -> {
			state.copy(backStackTabs = state.backStackTabs.subList(0, state.backStackTabs.indexOf(tab) + 1))
		}
		state.screens.keys.contains(tab) -> {
			state.copy(backStackTabs = state.backStackTabs + tab)
		}
		else -> {
			resolverScreenByTarget(target)?.let { screen ->
				state.copy(
					backStackTabs = state.backStackTabs + tab,
					screens = state.screens + (tab to listOf(screen))
				)
			} ?: state
		}
	}
}

internal fun <A : Any, I : Any, C : ScreenContract<A, I>> ScreensRegistry.pushInTab(
    tab: Tab,
    target: Target<A, I, C>
) = Action { state ->
    resolverScreenByTarget(target)?.let { screen ->
        when {
            state.currentTab == tab -> {
                state.copyOnCurrentScreens { it + screen }
            }
            state.backStackTabs.contains(tab) -> {
                state.copy(
                    backStackTabs = state.backStackTabs.subList(0, state.backStackTabs.indexOf(tab) + 1),
                ).copyOnCurrentScreens { it + screen }
            }
            state.screens.keys.contains(tab) -> {
                state.copy(
                    backStackTabs = state.backStackTabs + tab
                ).copyOnCurrentScreens { it + screen }
            }
            else -> {
                state.copy(
                    backStackTabs = state.backStackTabs + tab,
                    screens = state.screens + (tab to listOf(screen))
                )
            }
        }
    } ?: state
}

internal fun ScreensRegistry.popTab(tab: Tab) =
	Action { state ->
		if (!state.backStackTabs.contains(tab)) return@Action state

        state.currentTabScreens.firstScreenInState().afterCloseTarget
            ?.let { target -> resolverScreenByTarget(target = target, withAnswerTarget = false) }
            ?.let { screen -> return@Action state.copyOnCurrentScreens { listOf(screen) } }

        if (state.backStackTabs.size == 1) {
			clear().act(state)
		} else {
			state.copy(
				backStackTabs = state.backStackTabs.minus(tab),
				screens = HashMap(state.screens).apply { remove(tab) }
			)
		}
	}

internal fun <A : Any, I : Any, C : ScreenContract<A, I>> ScreensRegistry.push(target: Target<A, I, C>) =
	Action { state ->
		resolverScreenByTarget(target)?.let { new -> state.copyOnCurrentScreens { it + new } } ?: state
	}

internal fun ScreensRegistry.pop() =
	Action { state ->
        state.currentTabScreens.lastScreenInState().afterCloseTarget
            ?.let { target -> resolverScreenByTarget(target = target, withAnswerTarget = false) }
            ?.let { screen -> return@Action state.copyOnCurrentScreens { it.dropLast(1) + screen } }

		if (state.currentTabScreens.size == 1) {
			popTab(state.currentTab).act(state)
		} else {
			state.copyOnCurrentScreens { it.dropLast(1) }
		}
	}

internal fun clear() = Action { null }
