package com.parimatch.navigation.callback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberUpdatedState
import com.parimatch.navigation.receiver.Target
import com.parimatch.navigation.reflection.LocalScreenUidOwner

internal fun interface NavigationCallbackRegistryOwner {
    fun getNavigationCallbackRegistry(): NavigationCallbackRegistry
}

internal object LocalNavigationCallbackRegistryOwner {
    private val LocalComposition = compositionLocalOf<NavigationCallbackRegistryOwner?> { null }

    internal val current: NavigationCallbackRegistryOwner?
        @Composable
        get() = LocalComposition.current

    internal infix fun provides(registryOwner: NavigationCallbackRegistryOwner):
        ProvidedValue<NavigationCallbackRegistryOwner?> {
        return LocalComposition.provides(registryOwner)
    }
}

@Composable
public fun rememberNavigationCallbackReturnStub(
    onScreenReturned: (Target<*, *, *>) -> Unit
) : NavigationCallbackReturnStub {
    val currentOnScreenReturned = rememberUpdatedState(onScreenReturned)

    val screenUid = checkNotNull(LocalScreenUidOwner.current) {
        "No ScreenUidOwner was provided via LocalScreenUidOwner"
    }.getScreenUid()

    val navigationCallbackRegistry = checkNotNull(LocalNavigationCallbackRegistryOwner.current) {
        "No NavigationCallbackRegistryOwner was provided via LocalNavigationCallbackRegistryOwner"
    }.getNavigationCallbackRegistry()

    DisposableEffect(screenUid, navigationCallbackRegistry) {
        navigationCallbackRegistry.register(screenUid,
            object : NavigationCallback {
                override fun onScreenReturned(target: Target<*, *, *>) { currentOnScreenReturned.value(target) }
            }
        )
        onDispose {
            navigationCallbackRegistry.unregister(screenUid)
        }
    }
    return NavigationCallbackReturnStub
}

public object NavigationCallbackReturnStub