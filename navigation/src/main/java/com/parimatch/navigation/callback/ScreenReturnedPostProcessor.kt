package com.parimatch.navigation.callback

import com.parimatch.navigation.receiver.Target
import com.parimatch.navigation.screen.Screen
import com.parimatch.navigation.state.Action
import com.parimatch.navigation.state.PostProcessor
import com.parimatch.navigation.state.State
import com.parimatch.navigation.state.lastScreenInState
import kotlinx.coroutines.channels.SendChannel

internal class ScreenReturnedPostProcessor(
    private val callbackRegistry: NavigationCallbackRegistry
) : PostProcessor {

    override fun invoke(old: State?, new: State, channel: SendChannel<Action>) {
        if (old == null || old == new) return

        val currentScreen = new.currentTabScreens.lastScreenInState()
        val oldCurrentScreens = old.screens[new.currentTab] ?: return
        var closedScreen: Screen<*>? = oldCurrentScreens
            .indexOfFirst { it.uid == currentScreen.uid }
            .let { if (it == -1) return else it }
            .takeUnless { it == oldCurrentScreens.size - 1 }
            ?.let { oldCurrentScreens[it + 1] }

        // if (closedScreen == null) {
        //     val oldBackStack = old.backStackTabs
        //     closedScreen = oldBackStack
        //         .indexOfFirst { it == new.currentTab }
        //         .let { if (it == -1) return else it }
        //         .takeUnless { it == oldBackStack.size - 1 }
        //         ?.let { oldBackStack.subList(it + 1, oldBackStack.size) }
        //         ?.filterNot { new.screens.containsKey(it) }
        //         ?.getOrNull(0)
        //         ?.let { old.screens[it]?.firstScreen() }
        // }

        closedScreen?.run {
            Target::class.java
                .constructors[0]
                .newInstance(description.clazz, arg, context) as Target<*, *, *>
        }?.let { target ->
            callbackRegistry.sendEvent(NavigationEvent.ScreenReturned(currentScreen.uid, target))
        }
    }
}