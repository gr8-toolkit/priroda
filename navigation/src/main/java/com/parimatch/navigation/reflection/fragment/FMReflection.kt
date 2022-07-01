package com.parimatch.navigation.reflection.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleCoroutineScope
import com.parimatch.navigation.callback.NavigationCallback
import com.parimatch.navigation.callback.NavigationCallbackRegistry
import com.parimatch.navigation.callback.NavigationEvent
import com.parimatch.navigation.reflection.AbstractReflection
import com.parimatch.navigation.reflection.Form
import com.parimatch.navigation.reflection.ReflectionHolder
import com.parimatch.navigation.reflection.StateBundler
import com.parimatch.navigation.screen.Screen
import com.parimatch.navigation.state.State
import com.parimatch.navigation.state.Tab
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

internal val NULL_TAB = Tab("null-tab")

internal const val NULL_SCREEN_UID = "null-screen-uid"

internal abstract class FMReflection (
    private val screenResolver: ComposableScreenResolver,
    private val callbackRegistry: NavigationCallbackRegistry,
    source: (initial: State?) -> StateFlow<State?>,
    navigateBack: () -> Unit,
    bundler: StateBundler?
) : AbstractReflection<FragmentContainerView>(source, navigateBack, bundler) {

    companion object {
        internal const val CONTRACT_EXCEPTION_MESSAGE_1 = "This FragmentManager have been used before"
        internal const val CONTRACT_EXCEPTION_MESSAGE_2 = "FMReflection is not the only user of this FragmentManager"
        internal const val CONTRACT_EXCEPTION_MESSAGE_3 = "Tab name cannot be \"null-tab\" while using FragmentReflection"
    }

    override fun ReflectionHolder.constructView(context: Context): FragmentContainerView {
        fragmentManager.fragmentFactory = provideFragmentFactory()
        reflexionScope.launchNavigationEvents(fragmentManager)
        return FragmentContainerView(context)
    }

    private fun provideFragmentFactory(): FragmentFactory {
        return object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
                when (loadFragmentClass(classLoader, className)) {
                    ComposableFragment::class.java -> ComposableFragment(
                        callbackRegistry, screenResolver
                    )
                    else -> super.instantiate(classLoader, className)
                }
        }
    }

    private fun LifecycleCoroutineScope.launchNavigationEvents(fm: FragmentManager) {
        launchWhenStarted {
            callbackRegistry.navigationEvents.collect { event ->
                when (event) {
                    is NavigationEvent.ScreenReturned -> {
                        (fm.findFragmentByTag(event.uid) as? NavigationCallback)
                            ?.onScreenReturned(event.target)
                    }
                }
            }
        }
    }

    protected fun FragmentTransaction.addScreen(containerViewId: Int, screen: Screen<out Any>) {
        when (val form = screen.form) {
            is Form.Fragment -> {
                add(containerViewId, form.fragment, screen.uid)
            }
            is Form.Compose -> {
                val bundle = ComposableFragment.bundle(
                    screen.uid, screen.description.clazz, screen.arg
                )
                add(containerViewId, ComposableFragment::class.java, bundle, screen.uid)
            }
        }
    }
}
