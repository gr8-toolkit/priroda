package com.parimatch.navigation.screen

import com.parimatch.navigation.receiver.Target
import com.parimatch.navigation.reflection.Form
import com.parimatch.navigation.registry.ScreensModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass

internal val NO_LIMITATION = MutableStateFlow(true)

/**
 * Using produceForm or produceLimitation is a good question.
 */
public data class ScreenDescription<A : Any, I : Any, C : ScreenContract<A, I>> internal constructor(
    val module: ScreensModule,
    val clazz: KClass<C>,
    val produceForm: (A) -> Form,
    val name: String?,
    val defaultContext: ScreenContext,
    /** Alternative screen identifiers in deeplink resolve process. */
    val internalLinkAgent: ScreenInternalLinkAgent<A>?,
    /** Field for screen related information. */
    val info: (A) -> Flow<I>,
    val produceLimitation: (A) -> StateFlow<Boolean>, // todo relocate to separate part
    val limitationAnswerTarget: Target<*, *, *>? // todo relocate to separate part
)
