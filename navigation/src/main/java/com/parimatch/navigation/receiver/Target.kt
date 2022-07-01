package com.parimatch.navigation.receiver

import com.parimatch.navigation.callback.ScreenReturnedPostProcessor
import com.parimatch.navigation.screen.ScreenContext
import com.parimatch.navigation.screen.ScreenContract
import kotlin.reflect.KClass
import com.parimatch.navigation.reflection.StateBundler

/**
 * Reflection constructor in [InternalLinkNavigationActionMapper], [ScreenReturnedPostProcessor],
 * [StateBundler].
 */
public data class Target<A : Any, I : Any, C : ScreenContract<A, I>>(
	val clazz: KClass<C>,
	val arg: A,
	val context: ScreenContext? = null
)
