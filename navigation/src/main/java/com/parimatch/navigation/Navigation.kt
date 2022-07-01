package com.parimatch.navigation

import com.parimatch.navigation.callback.ScreenReturnedPostProcessor
import com.parimatch.navigation.callback.NavigationCallbackRegistry
import com.parimatch.navigation.limitation.LimitationPostProcessor
import com.parimatch.navigation.receiver.InternalLinkNavigationActionMapper
import com.parimatch.navigation.receiver.InternalLinkNavigationReceiver
import com.parimatch.navigation.receiver.NavigationReceiver
import com.parimatch.navigation.reflection.compose.ComposeReflection
import com.parimatch.navigation.reflection.Form
import com.parimatch.navigation.reflection.Reflection
import com.parimatch.navigation.reflection.StateBundler
import com.parimatch.navigation.reflection.fragment.ComposableScreenResolver
import com.parimatch.navigation.reflection.fragment.backstack.FMBackStackReflection
import com.parimatch.navigation.reflection.fragment.hide.FMHideReflection
import com.parimatch.navigation.registry.ScreensModule
import com.parimatch.navigation.registry.ScreensRegistry
import com.parimatch.navigation.screen.*
import com.parimatch.navigation.screen.Screen
import com.parimatch.navigation.state.NavigationStateMachine
import com.parimatch.navigation.state.State
import com.parimatch.navigation.state.Tab
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import java.util.*

public interface NavigationInterfaces : AutoCloseable {

	public val registry: ScreensRegistry

	public val internalLinkReceiver: InternalLinkNavigationReceiver

	public val receiver: NavigationReceiver

	public val reflection: Reflection
}

public class Navigation(private val name: String) {

	private var coroutineScope: CoroutineScope? = null

	private var stateMachine: NavigationStateMachine? = null

	/**
	 *
	 */
	private var mapper: InternalLinkNavigationActionMapper? = null

	public val internalLinkReceiver: InternalLinkNavigationReceiver? get() = mapper

	public val receiver: NavigationReceiver? get() = mapper

	/**
	 *
	 */
	public var registry: ScreensRegistry? = null
		private set

	/**
	 *
	 */
	public var reflection: Reflection? = null
		private set

	public fun setup(
		composeOnly: Boolean,
        undeadFragments: Boolean = false,
        saveNavigationState: Boolean = true
	): NavigationInterfaces {
		val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }
		coroutineScope = CoroutineScope(
			CoroutineName("${this::class.java.simpleName} $name") +
					Dispatchers.Main +
					SupervisorJob() +
					coroutineExceptionHandler
		)
		val limitationPostProcessor = LimitationPostProcessor(coroutineScope!!)
        val callbackRegistry = NavigationCallbackRegistry()
        val screenReturnedPostProcessor = ScreenReturnedPostProcessor(callbackRegistry)
		stateMachine = NavigationStateMachine(
			coroutineScope = coroutineScope!!,
			postProcessors = listOf(
                limitationPostProcessor,
                screenReturnedPostProcessor
            )
		)

		mapper = InternalLinkNavigationActionMapper(stateMachine!!::sendAction)
		registry = ScreensRegistry(mapper!!)
        limitationPostProcessor.provideRegistry(registry!!)
		mapper!!.provideRegistry(registry!!)

		val stateFactory: (State?) -> StateFlow<State?> = {
			stateMachine!!.handleActions(it ?: registry!!.generateInitialState())
		}
		val navigateBack: () -> Unit = { mapper!!.back() }
		val bundler = if (saveNavigationState) StateBundler(registry!!) else null
        val screenResolver = ComposableScreenResolver(registry!!)
        val reflectionFactory =
            when {
                composeOnly -> ::ComposeReflection
                undeadFragments -> ::FMHideReflection
                else -> ::FMBackStackReflection
            }
        reflection = reflectionFactory(
            screenResolver,
            callbackRegistry,
            stateFactory,
            navigateBack,
            bundler
        )

		return object : NavigationInterfaces {
            override val registry: ScreensRegistry get() =
                this@Navigation.registry ?: throw IllegalStateException(CONTRACT_EXCEPTION_MESSAGE_1)

            override val internalLinkReceiver: InternalLinkNavigationReceiver get() =
                this@Navigation.internalLinkReceiver ?: throw IllegalStateException(CONTRACT_EXCEPTION_MESSAGE_1)

            override val receiver: NavigationReceiver get() =
                this@Navigation.receiver ?: throw IllegalStateException(CONTRACT_EXCEPTION_MESSAGE_1)

            override val reflection: Reflection get() =
                this@Navigation.reflection ?: throw IllegalStateException(CONTRACT_EXCEPTION_MESSAGE_1)

			override fun close() {
				this@Navigation.reflection = null
				this@Navigation.registry = null
				mapper = null
				stateMachine = null
				coroutineScope?.cancel()
				coroutineScope = null
			}
		}
	}

	private interface InitialScreen : BlankScreenContract

	internal companion object {

        private const val CONTRACT_EXCEPTION_MESSAGE_1 = "Using Navigation after closing"

        @Suppress("UNCHECKED_CAST")
		internal fun ScreensRegistry.generateInitialState(): State {
			val tab = UUID.randomUUID().toString()
			val description: ScreenDescription<Unit, Unit, InitialScreen> = with(
				object : ScreensModule() {
					override val id: String get() = this::class.java.simpleName
					override fun Storage.fulfillScreens() {
						screen<InitialScreen>(InitialScreen::class, { Form.Compose {
							//Text(text = "test", color = Color.Red)
						} })
					}
				}
			) {
				val storage = ScreensModule.Storage(this)
				storage.fulfillScreens()
				storage.screens.first() as ScreenDescription<Unit, Unit, InitialScreen>
			}
			register(description)
			val screen = Screen(
				uid = UUID.randomUUID().toString(),
				context = ScreenContext(Place.DEFAULT, null),
				description = description,
				arg = Unit
			)
			return State(
				backStackTabs = listOf(Tab(tab)),
				screens = mapOf(Tab(tab) to listOf(screen))
			)
		}
	}
}
