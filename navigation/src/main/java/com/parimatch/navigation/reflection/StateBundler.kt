package com.parimatch.navigation.reflection

import android.os.Bundle
import androidx.core.os.bundleOf
import com.parimatch.navigation.receiver.Target
import com.parimatch.navigation.registry.ScreensRegistry
import com.parimatch.navigation.screen.*
import com.parimatch.navigation.screen.Screen
import com.parimatch.navigation.state.State
import com.parimatch.navigation.state.Tab
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.ArrayList
import kotlin.reflect.KClass

internal class StateBundler(private val registry: ScreensRegistry) {

	companion object {
		private const val BACK_STACK_TABS_TAG = "back-stack-tabs"
		private const val TABS_TAG = "tabs"
		private const val SCREEN_ID_TAG = "screen-id"
		private const val SCREEN_CONTEXT_TAG = "screen-context"
		private const val SCREEN_DESCRIPTION_CLASS_TAG = "screen-description-class"
		private const val SCREEN_ARG_TAG = "screen-arg"
        private const val SCREEN_AFTER_CLOSE_INTENT_TAG = "screen-after-close-intent"

        private const val INTENT_CLASS_TAG = "intent-class"
        private const val INTENT_ARG_TAG = "intent-arg"
        private const val INTENT_CONTEXT_TAG = "intent-context"
	}

	fun toBundle(state: State): Bundle = Bundle().apply {
		putStringArray(BACK_STACK_TABS_TAG, state.backStackTabs.map { it.value }.toTypedArray())
		putStringArray(TABS_TAG, state.screens.keys.map { it.value }.toTypedArray())
		state.screens.forEach { entry ->
			val screens = ArrayList<Bundle>().apply {
				entry.value.forEach { screen ->
					val bundle = runCatching {
						bundleOf(
							SCREEN_ID_TAG to screen.uid,
							SCREEN_CONTEXT_TAG to screen.context,
							SCREEN_DESCRIPTION_CLASS_TAG to screen.description.clazz.java,
							SCREEN_ARG_TAG to if(screen.arg is Unit) null else screen.arg,
                            SCREEN_AFTER_CLOSE_INTENT_TAG to screen.afterCloseTarget?.run {
                                bundleOf(
                                    INTENT_CLASS_TAG to clazz,
                                    INTENT_ARG_TAG to arg,
                                    INTENT_CONTEXT_TAG to context
                                )
                            }
						)
					}.getOrElse {
						throw IllegalArgumentException("The argument is not yet a supported type")
					}
					add(bundle)
				}
			}
			putParcelableArrayList(entry.key.value, screens)
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun fromBundle(bundle: Bundle): State? {
		if (bundle.isEmpty) return null
		val backStackTabs: List<Tab> = bundle.getStringArray(BACK_STACK_TABS_TAG)?.map { Tab(it) } ?: return null
		val tabs = bundle.getStringArray(TABS_TAG)?.map { Tab(it) } ?: return null
		val screens: Map<Tab, List<Screen<out Any>>> = tabs.map { tab ->
			tab to (bundle.getParcelableArrayList<Bundle>(tab.value)?.map { screenFromBundle(it) } ?: return null)
		}.toMap()
		return State(backStackTabs, screens)
	}

	@Suppress("UNCHECKED_CAST")
	private fun screenFromBundle(bundle: Bundle): Screen<*> {
		val uid: String = bundle.getString(SCREEN_ID_TAG)!!
		val context: ScreenContext = bundle.get(SCREEN_CONTEXT_TAG)!! as ScreenContext
		val descriptionKClass: KClass<out ScreenContract<*, *>> =
			(bundle.get(SCREEN_DESCRIPTION_CLASS_TAG)!! as Class<out ScreenContract<*, *>>).kotlin
		val description: ScreenDescription<*, *, *> = registry.resolveScreenRaw(descriptionKClass)
			?: throw IllegalStateException("StateBundler was built on wrong ScreenRegistry")
		val arg: Any = bundle.get(SCREEN_ARG_TAG) ?: Unit
		val afterCloseTarget = bundle.getBundle(SCREEN_AFTER_CLOSE_INTENT_TAG)?.let { targetBundle ->
            Target::class.java.constructors[0].newInstance(
                (bundle.get(INTENT_CLASS_TAG)!! as Class<out ScreenContract<*, *>>).kotlin,
                bundle.get(INTENT_ARG_TAG) ?: Unit,
                targetBundle.get(INTENT_CONTEXT_TAG)!! as ScreenContext
            )
        }
        return Screen::class.java.constructors[0].newInstance(
            uid,
            context,
            description,
            arg,
            afterCloseTarget
        ) as Screen<*>
	}
}
