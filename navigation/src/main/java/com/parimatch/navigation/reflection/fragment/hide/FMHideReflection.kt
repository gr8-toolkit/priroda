package com.parimatch.navigation.reflection.fragment.hide

import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commitNow
import com.parimatch.navigation.callback.NavigationCallbackRegistry
import com.parimatch.navigation.reflection.Reflection
import com.parimatch.navigation.reflection.ReflectionHolder
import com.parimatch.navigation.reflection.StateBundler
import com.parimatch.navigation.reflection.fragment.ComposableScreenResolver
import com.parimatch.navigation.reflection.fragment.FMReflection
import com.parimatch.navigation.reflection.fragment.NULL_TAB
import com.parimatch.navigation.state.State
import com.parimatch.navigation.state.Tab
import kotlinx.coroutines.flow.StateFlow

internal class FMHideReflection(
    screenResolver: ComposableScreenResolver,
    callbackRegistry: NavigationCallbackRegistry,
    source: (initial: State?) -> StateFlow<State?>,
    navigateBack: () -> Unit,
    bundler: StateBundler?
) : FMReflection(screenResolver, callbackRegistry, source, navigateBack, bundler) {

    override fun FragmentContainerView.reflect(state: State, holder: ReflectionHolder) {
        val currentTab: Tab = state.currentTab
        if (currentTab == NULL_TAB) throw IllegalArgumentException(CONTRACT_EXCEPTION_MESSAGE_3)
        val fm = holder.fragmentManager
        val new = FMHideInternalState.equalFromState(state)
        val old = (getTag(Reflection.CONTAINER_TAG_KEY) as? FMHideInternalState)
            ?: run { // newly added reflection
                FMHideInternalState.restoreFromFragmentManager(fm)
            }
        val screens = state.screens.values.flatten()

        val toRemove = old.screens - new.screens
        toRemove.onEach { uid ->
            val fragment = fm.findFragmentByTag(uid) ?: throw IllegalArgumentException(CONTRACT_EXCEPTION_MESSAGE_2)
            fm.commitNow(allowStateLoss = true) { remove(fragment) }
        }

        (new.screens - old.screens).onEach { uid ->
            val screen = screens.find { it.uid == uid } ?: throw IllegalArgumentException(CONTRACT_EXCEPTION_MESSAGE_2)
            fm.commitNow(allowStateLoss = true) {
                addScreen(id, screen)
            }
        }

        (old.shown - toRemove - new.shown).onEach { uid ->
            val fragment = fm.findFragmentByTag(uid) ?: throw IllegalArgumentException(CONTRACT_EXCEPTION_MESSAGE_2)
            fm.commitNow(allowStateLoss = true) { hide(fragment) }
        }

        (new.shown - old.shown).onEach { uid ->
            val fragment = fm.findFragmentByTag(uid) ?: throw IllegalArgumentException(CONTRACT_EXCEPTION_MESSAGE_2)
            fm.commitNow(allowStateLoss = true) { show(fragment) }
        }

        setTag(Reflection.CONTAINER_TAG_KEY, new)
    }
}