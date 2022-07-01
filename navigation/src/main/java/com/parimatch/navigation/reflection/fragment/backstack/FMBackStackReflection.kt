package com.parimatch.navigation.reflection.fragment.backstack

import androidx.fragment.app.*
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import com.parimatch.navigation.callback.NavigationCallbackRegistry
import com.parimatch.navigation.reflection.StateBundler
import com.parimatch.navigation.reflection.Reflection.Companion.CONTAINER_TAG_KEY
import com.parimatch.navigation.reflection.ReflectionHolder
import com.parimatch.navigation.reflection.fragment.ComposableScreenResolver
import com.parimatch.navigation.reflection.fragment.FMReflection
import com.parimatch.navigation.reflection.fragment.NULL_TAB
import com.parimatch.navigation.screen.Screen
import com.parimatch.navigation.state.State
import com.parimatch.navigation.state.Tab
import com.parimatch.navigation.state.firstScreenInState
import com.parimatch.navigation.state.handleScreensDifference
import kotlinx.coroutines.flow.StateFlow
import java.lang.IllegalArgumentException

internal class FMBackStackReflection(
	screenResolver: ComposableScreenResolver,
    callbackRegistry: NavigationCallbackRegistry,
	source: (initial: State?) -> StateFlow<State?>,
	navigateBack: () -> Unit,
	bundler: StateBundler?
) : FMReflection(screenResolver, callbackRegistry, source, navigateBack, bundler) {

	/**
	 * ### Rules:
	 * * Single [FragmentTransaction.addToBackStack] with name at head of tab.
	 * * Single [Fragment] per [FragmentTransaction].
	 */
	@Suppress("UNCHECKED_CAST")
	override fun FragmentContainerView.reflect(
		state: State,
		holder: ReflectionHolder
	) {
		val currentTab: Tab = state.currentTab
		if (currentTab == NULL_TAB) throw IllegalArgumentException(CONTRACT_EXCEPTION_MESSAGE_3)
		val currentTabScreens = state.currentTabScreens
		val fm = holder.fragmentManager
		val old = (getTag(CONTAINER_TAG_KEY) as? FMBackStackInternalState)
			?: run { // newly added reflection
				FMBackStackInternalState.restoreFromFragmentManager(fm)
			}
		if (FMBackStackInternalState.equalFromState(state) == old) return

		val present: List<String> =
			if (old == null) {
				emptyList()
			} else {
				(old.screens.keys - old.currentTab - state.screens.keys).forEach {
					fm.clearBackStack(it.value)
				}

				old.currentTab.takeUnless { it == currentTab }?.also {
					when {
						state.screens.keys.contains(it) -> {
							fm.saveBackStack(it.value)
						}
						it == NULL_TAB -> {
							repeat(fm.backStackEntryCount) { fm.popBackStack() }
						}
						else -> {
							fm.popBackStack(it.value, POP_BACK_STACK_INCLUSIVE)
						}
					}
				}

				old.screens[currentTab]?.let { restored ->
					fm.restoreBackStack(currentTab.value)
					restored
				} ?: emptyList()
			}

		currentTabScreens.handleScreensDifference(present)() {
			fm.popBackStack()
		}() { screen ->
			if (screen.uid == currentTabScreens.firstScreenInState().uid) {
				fm.commitScreen(id, screen, currentTab)
			} else {
				fm.commitScreen(id, screen)
			}
		}

		val new = old?.straightenOnCurrentTab(state)
			?: FMBackStackInternalState.currentTabFromState(state)
		setTag(CONTAINER_TAG_KEY, new)
	}

	private fun FragmentManager.commitScreen(containerViewId: Int, screen: Screen<out Any>, tab: Tab? = null) {
		commit (allowStateLoss = true) {
			setReorderingAllowed(true)
            addToBackStack(tab?.value)
            addScreen(containerViewId, screen)
        }
	}
}
