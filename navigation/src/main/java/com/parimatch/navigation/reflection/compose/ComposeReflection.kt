package com.parimatch.navigation.reflection.compose

import android.content.Context
import androidx.compose.ui.platform.ComposeView
import com.parimatch.navigation.callback.NavigationCallbackRegistry
import com.parimatch.navigation.reflection.AbstractReflection
import com.parimatch.navigation.reflection.StateBundler
import com.parimatch.navigation.reflection.ReflectionHolder
import com.parimatch.navigation.reflection.fragment.ComposableScreenResolver
import com.parimatch.navigation.state.State
import kotlinx.coroutines.flow.StateFlow

internal class ComposeReflection(
    private val screenResolver: ComposableScreenResolver,
    private val callbackRegistry: NavigationCallbackRegistry,
	source: (initial: State?) -> StateFlow<State?>,
	navigateBack: () -> Unit,
	bundler: StateBundler?
) : AbstractReflection<ComposeView>(source, navigateBack, bundler) {

	override fun ReflectionHolder.constructView(context: Context): ComposeView =
		ComposeView(context)

	override fun ComposeView.reflect(state: State, holder: ReflectionHolder) {
		TODO("Not yet implemented")
	}
}
