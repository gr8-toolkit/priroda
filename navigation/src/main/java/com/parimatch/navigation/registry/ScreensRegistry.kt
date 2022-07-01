package com.parimatch.navigation.registry

import com.parimatch.navigation.receiver.Target
import com.parimatch.navigation.receiver.InternalLinkNavigationReceiver
import com.parimatch.navigation.screen.Screen
import com.parimatch.navigation.screen.ScreenContract
import com.parimatch.navigation.screen.ScreenDescription
import kotlin.reflect.KClass

/* todo dynamic modules */
public class ScreensRegistry public constructor(
	private val receiver: InternalLinkNavigationReceiver
) {

	private val screens = mutableMapOf<KClass<*>, Any>()

	private val aliasedScreens = mutableMapOf<String, Any>()

	internal fun resolveScreenByAlias(alias: String): ScreenDescription<*, *, *>? {
		return aliasedScreens[alias]?.let { it as ScreenDescription<*, *, *> }
	}

	internal fun resolveScreenRaw(clazz: KClass<*>): ScreenDescription<*, *, *>? {
		return screens[clazz] as ScreenDescription<*, *, *>?
	}

	@Suppress("UNCHECKED_CAST")
	internal fun <A : Any, I : Any, C : ScreenContract<A, I>> resolveScreen(clazz: KClass<C>): ScreenDescription<A, I, C>? {
		return screens[clazz] as ScreenDescription<A, I, C>?
	}

    internal fun <A : Any, I : Any, C : ScreenContract<A, I>> resolverScreenByTarget(
        target: Target<A, I, C>,
        afterCloseTarget: Target<*, *, *>? = null,
        withAnswerTarget: Boolean = true
    ): Screen<out Any>? {
        val description = resolveScreen(target.clazz) ?: return null
        val screen = Screen.from(description, target, afterCloseTarget)
        return if (screen.limitation.value) screen
        else {
            description.limitationAnswerTarget
                ?.takeIf { withAnswerTarget }
                ?.let { answer -> resolverScreenByTarget(answer, target) }
        }
    }

	internal fun register(description: ScreenDescription<*, *, *>) {
		screens[description.clazz] = description
		description.internalLinkAgent?.aliases?.forEach {
			aliasedScreens[it] = description
		}
	}

	public fun register(vararg modules: ScreensModule) {
		modules.onEach { module ->
			module.register(receiver).onEach { register(it) }
		}
	}
}
