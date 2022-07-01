package com.parimatch.navigation.receiver

import com.parimatch.navigation.registry.ScreensRegistry
import com.parimatch.navigation.screen.ScreenContract
import com.parimatch.navigation.state.Action
import com.parimatch.navigation.state.Tab

internal open class NavigationActionMapper(
	private val sendAction: (Action) -> Unit
) : NavigationReceiver {

	protected var registry: ScreensRegistry? = null
		private set

	internal fun provideRegistry(screensRegistry: ScreensRegistry) {
		registry = screensRegistry
		/* todo action due to new registry */
	}

	override fun replaceRoot(tab: Tab, target: Target<out Any, out Any, out ScreenContract<out Any, out Any>>) =
		withRegistry { sendAction(replaceRootTab(tab, target)) }

	override fun openTab(tab: Tab, target: Target<out Any, out Any, out ScreenContract<out Any, out Any>>) =
		withRegistry { sendAction(pushTab(tab, target)) }

    override fun openInTab(tab: Tab, target: Target<out Any, out Any, out ScreenContract<out Any, out Any>>) =
        withRegistry { sendAction(pushInTab(tab, target)) }

	override fun closeTab(tab: Tab) {
        withRegistry { sendAction(popTab(tab)) }
	}

	override fun go(target: Target<out Any, out Any, out ScreenContract<out Any, out Any>>) =
		withRegistry { sendAction(push(target)) }

	override fun back() {
        withRegistry { sendAction(pop()) }
	}

	private inline fun withRegistry(block: ScreensRegistry.() -> Unit) {
		registry?.block() ?: throw IllegalStateException("ScreenRegistry was not set in NavigationActionMapper")
	}
}
