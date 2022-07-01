package com.parimatch.navigation.screen

import com.parimatch.navigation.receiver.Target
import com.parimatch.navigation.reflection.Form
import com.parimatch.navigation.reflection.StateBundler
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/**
 * Reflection constructor in [StateBundler].
 */
internal data class Screen<A : Any>(
    val uid: String,
    val context: ScreenContext,
    val description: ScreenDescription<A, *, *>,
    val arg: A,
    val afterCloseTarget: Target<*, *, *>? = null // todo relocate to separate part
) {

    val form: Form get() = description.produceForm(arg)

    val limitation: StateFlow<Boolean> get() = description.produceLimitation(arg)

    companion object {

        fun <A : Any, I : Any, C : ScreenContract<A, I>> from(
            screenDescription: ScreenDescription<A, I, C>,
            target: Target<A, I, C>,
            afterCloseTarget: Target<*, *, *>? = null
        ): Screen<A> {
            return Screen(
                uid = UUID.randomUUID().toString(),
                context = target.context ?: screenDescription.defaultContext,
                description = screenDescription,
                arg = target.arg,
                afterCloseTarget = afterCloseTarget
            )
        }
    }
}
