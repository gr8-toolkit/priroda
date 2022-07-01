package com.parimatch.navigation.registry

import com.parimatch.navigation.receiver.Target
import com.parimatch.navigation.receiver.InternalLinkNavigationReceiver
import com.parimatch.navigation.reflection.Form
import com.parimatch.navigation.screen.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.reflect.KClass

public abstract class ScreensModule {

	public abstract val id: String

	public abstract fun Storage.fulfillScreens()

	private var receiver: InternalLinkNavigationReceiver? = null
	public fun navigate(block: InternalLinkNavigationReceiver.() -> Unit) {
		receiver?.block()
	}

	public fun register(navigationReceiver: InternalLinkNavigationReceiver): List<ScreenDescription<out Any, out Any, out ScreenContract<out Any, out Any>>> {
		receiver = navigationReceiver
		val storage = Storage(this)
		storage.fulfillScreens()
		return storage.screens
	}

	public class Storage internal constructor(private val module: ScreensModule) {

		internal val screens = mutableListOf<ScreenDescription<out Any, out Any, out ScreenContract<out Any, out Any>>>()

        public fun <C : ScreenContract<Unit, Unit>> screen(
            clazz: KClass<C>,
            produceForm: () -> Form,
            name: String? = null,
            defaultContext: ScreenContext = ScreenContext(Place.DEFAULT, null),
            aliases: Set<String>? = null,
            produceLimitation: (Unit) -> StateFlow<Boolean> = { NO_LIMITATION },
            limitationAnswerTarget: Target<*, *, *>? = null
        ): ScreenDescription<Unit, Unit, C> =
            screen(
                clazz,
                { produceForm() },
                name,
                defaultContext,
                aliases?.let { ScreenInternalLinkAgent.argumentless(aliases) },
                { emptyFlow() },
                produceLimitation,
                limitationAnswerTarget
            )

        public fun <I : Any, C : ScreenContract<Unit, I>> screen(
            clazz: KClass<C>,
            produceForm: () -> Form,
            name: String? = null,
            defaultContext: ScreenContext = ScreenContext(Place.DEFAULT, null),
            aliases: Set<String>? = null,
            info: (Unit) -> Flow<I>,
            produceLimitation: (Unit) -> StateFlow<Boolean> = { NO_LIMITATION },
            limitationAnswerTarget: Target<*, *, *>? = null
        ): ScreenDescription<Unit, I, C> =
            screen(
                clazz,
                { produceForm() },
                name,
                defaultContext,
                aliases?.let { ScreenInternalLinkAgent.argumentless(aliases) },
                info,
                produceLimitation,
                limitationAnswerTarget
            )

		public fun <A : Any, C : ScreenContract<A, Unit>> screen(
			clazz: KClass<C>,
			produceForm: (A) -> Form,
			name: String? = null,
			defaultContext: ScreenContext = ScreenContext(Place.DEFAULT, null),
            internalLinkAgent: ScreenInternalLinkAgent<A>? = null,
			produceLimitation: (A) -> StateFlow<Boolean> = { NO_LIMITATION },
            limitationAnswerTarget: Target<*, *, *>? = null
		): ScreenDescription<A, Unit, C> =
			screen(
                clazz,
                produceForm,
                name,
                defaultContext,
                internalLinkAgent,
                { emptyFlow() },
                produceLimitation,
                limitationAnswerTarget
            )

		public fun <A : Any, I : Any, C : ScreenContract<A, I>> screen(
			clazz: KClass<C>,
			produceForm: (A) -> Form,
			name: String? = null,
			defaultContext: ScreenContext = ScreenContext(Place.DEFAULT, null),
            internalLinkAgent: ScreenInternalLinkAgent<A>? = null,
			info: (A) -> Flow<I>,
			produceLimitation: (A) -> StateFlow<Boolean> = { NO_LIMITATION },
            limitationAnswerTarget: Target<*, *, *>? = null
		): ScreenDescription<A, I, C> =
			ScreenDescription(
				module = module,
				clazz = clazz,
				produceForm = produceForm,
				name = name,
				defaultContext = defaultContext,
                internalLinkAgent = internalLinkAgent,
				info = info,
				produceLimitation = produceLimitation,
                limitationAnswerTarget = limitationAnswerTarget
			).also {
				screens.add(it)
			}
	}
}
