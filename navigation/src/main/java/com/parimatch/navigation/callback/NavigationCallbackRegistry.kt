package com.parimatch.navigation.callback

import com.parimatch.navigation.receiver.Target
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal sealed class NavigationEvent {
    data class ScreenReturned(val uid: String, val target: Target<*, *, *>) : NavigationEvent()
}

internal class NavigationCallbackRegistry {
    private val screenUidToCallback = mutableMapOf<String, NavigationCallback>()

    fun register(screenUid: String, callback: NavigationCallback) {
        screenUidToCallback[screenUid] = callback
    }

    fun unregister(screenUid: String) {
        screenUidToCallback.remove(screenUid)
    }

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigationEvents: SharedFlow<NavigationEvent> get() = _navigationEvents

    fun sendEvent(event: NavigationEvent) {
        _navigationEvents.tryEmit(event)
        when (event) {
            is NavigationEvent.ScreenReturned -> {
                screenUidToCallback[event.uid]?.onScreenReturned(event.target)
            }
        }
    }
}

