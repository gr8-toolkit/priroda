package com.parimatch.navigation.callback

import com.parimatch.navigation.receiver.Target

public interface NavigationCallback {
    public fun onScreenReturned(target: Target<*, *, *>)
}