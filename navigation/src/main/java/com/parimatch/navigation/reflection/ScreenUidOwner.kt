package com.parimatch.navigation.reflection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf

internal fun interface ScreenUidOwner {
    fun getScreenUid(): String
}

internal object LocalScreenUidOwner {
    private val LocalComposition = compositionLocalOf<ScreenUidOwner?> { null }

    internal val current: ScreenUidOwner?
        @Composable
        get() = LocalComposition.current

    internal infix fun provides(registryOwner: ScreenUidOwner):
        ProvidedValue<ScreenUidOwner?> {
        return LocalComposition.provides(registryOwner)
    }
}