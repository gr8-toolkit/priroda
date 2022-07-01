package com.parimatch.navigation.receiver

import com.parimatch.navigation.screen.ScreenContext
import com.parimatch.navigation.screen.ScreenContract
import com.parimatch.navigation.state.Tab
import kotlin.reflect.KClass

public interface NavigationReceiver {

	/**
	 * Because there cannot be blank list of tabs, we should replace it anyway.
	 */
	public fun <A : Any, I : Any, C : ScreenContract<A, I>> replaceRoot(
		tab: Tab,
		clazz: KClass<C>,
		arg: A,
		context: ScreenContext? = null
	){ replaceRoot(tab, Target(clazz, arg, context)) }

	public fun replaceRoot(
		tab: Tab,
		target: Target<out Any, out Any, out ScreenContract<out Any, out Any>>
	)

	public fun <I : Any, C : ScreenContract<Unit, I>> openTab(
		tab: Tab,
		clazz: KClass<C>,
		context: ScreenContext? = null
	) {
		openTab(tab, Target(clazz, Unit, context))
	}

	public fun <A : Any, I : Any, C : ScreenContract<A, I>> openTab(
		tab: Tab,
		clazz: KClass<C>,
		arg: A,
		context: ScreenContext? = null
	) {
		openTab(tab, Target(clazz, arg, context))
	}

	public fun openTab(
		tab: Tab,
		target: Target<out Any, out Any, out ScreenContract<out Any, out Any>>
	)

    public fun <I : Any, C : ScreenContract<Unit, I>> openInTab(
        tab: Tab,
        clazz: KClass<C>,
        context: ScreenContext? = null
    ) { openInTab(tab, Target(clazz, Unit, context)) }

    public fun <A : Any, I : Any, C : ScreenContract<A, I>> openInTab(
        tab: Tab,
        clazz: KClass<C>,
        arg: A,
        context: ScreenContext? = null
    ) { openInTab(tab, Target(clazz, arg, context)) }

    public fun openInTab(
        tab: Tab,
        target: Target<out Any, out Any, out ScreenContract<out Any, out Any>>
    )

	public fun closeTab(tab: Tab)

	public fun chain(targets: List<Target<out Any, out Any, out ScreenContract<out Any, out Any>>>) {
		targets.forEach { go(it) }
	}

	public fun <I : Any, C : ScreenContract<Unit, I>> go(
		clazz: KClass<C>,
		context: ScreenContext? = null
	) {
		go(Target(clazz, Unit, context))
	}

	public fun <A : Any, I : Any, C : ScreenContract<A, I>> go(
		clazz: KClass<C>,
		arg: A,
		context: ScreenContext? = null
	) {
		go(Target(clazz, arg, context))
	}

	public fun go(target: Target<out Any, out Any, out ScreenContract<out Any, out Any>>)

	public fun back()

	/* close chain */

	/* for result */
}
