package com.parimatch.navigation.reflection

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistry
import com.parimatch.navigation.R
import com.parimatch.navigation.screen.Place
import com.parimatch.navigation.state.State
import com.parimatch.navigation.state.Tab
import com.parimatch.navigation.state.lastScreenInState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

public interface ReflectionHolder : ReflectionCallbacks {

	public val fragmentManager: FragmentManager

	public val reflexionScope: LifecycleCoroutineScope

	public val onBackPressedDispatcher: OnBackPressedDispatcher

	public val savedStateRegistry: SavedStateRegistry

	public val lifecycleOwner: LifecycleOwner
}

public interface ReflectionCallbacks {

	public fun onEnvironmentChange(currentTab: Tab, isFullscreen: Boolean) {}
}

public interface Reflection {

	public companion object {
		public val CONTAINER_TAG_KEY: Int = R.string.container_tag_ket
	}

	public fun <A> A.view(context: Context): View where A : AppCompatActivity, A : ReflectionCallbacks =
		object : ReflectionHolder {
			override val fragmentManager: FragmentManager
				get() = this@view.supportFragmentManager
			override val reflexionScope: LifecycleCoroutineScope
				get() = this@view.lifecycleScope
			override val onBackPressedDispatcher: OnBackPressedDispatcher
				get() = this@view.onBackPressedDispatcher
			override val savedStateRegistry: SavedStateRegistry
				get() = this@view.savedStateRegistry
			override val lifecycleOwner: LifecycleOwner
				get() = this@view

			override fun onEnvironmentChange(currentTab: Tab, isFullscreen: Boolean) {
				this@view.onEnvironmentChange(currentTab, isFullscreen)
			}
		}.view(context)

	public fun ReflectionHolder.view(context: Context): View
}

internal abstract class AbstractReflection<V : View>(
	private val source: (initial: State?) -> StateFlow<State?>,
	private val navigateBack: () -> Unit,
	private val bundler: StateBundler?
) : Reflection {

	companion object {
		private const val NAVIGATION_TAG = "navigation-tag"
	}

	protected abstract fun ReflectionHolder.constructView(context: Context): V

	protected abstract fun V.reflect(state: State, holder: ReflectionHolder)

	override fun ReflectionHolder.view(context: Context): View {
		onBackPressedDispatcher.addCallback(lifecycleOwner, object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() { navigateBack() }
		})
		val view: V = constructView(context)
		reflexionScope.launchWhenStarted {
			val source = source(
                bundler?.let {
                    savedStateRegistry.consumeRestoredStateForKey(NAVIGATION_TAG)?.let { bundler.fromBundle(it) }
                }
            )

            if (bundler != null) {
                savedStateRegistry.unregisterSavedStateProvider(NAVIGATION_TAG)
                savedStateRegistry.registerSavedStateProvider(NAVIGATION_TAG) {
                    source.value?.let { bundler.toBundle(it) } ?: Bundle()
                }
            }

			source.collect { new ->
				new?.let {
					view.reflect(new, this@view)
					onEnvironmentChange(
                        new.currentTab,
                        new.currentTabScreens.lastScreenInState().context.place == Place.FULLSCREEN
                    )
				} ?: run {
					(context as? Activity)?.finish()
				}
			}
		}
		return view
	}
}
