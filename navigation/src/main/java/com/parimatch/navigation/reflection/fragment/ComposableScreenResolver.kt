package com.parimatch.navigation.reflection.fragment

import androidx.compose.runtime.Composable
import com.parimatch.navigation.reflection.Form
import com.parimatch.navigation.registry.ScreensRegistry
import java.lang.IllegalStateException
import kotlin.reflect.KClass

internal class ComposableScreenResolver (private val registry: ScreensRegistry) {

	@Suppress("UNCHECKED_CAST")
	fun get(clazz: KClass<*>, arg: Any): @Composable () -> Unit {
		val composable = registry.resolveScreenRaw(clazz)?.produceForm
			?.let { (it as Function1<Any, Form>).invoke(arg) }
			?.let { (it as Form.Compose).composable }
		if (composable != null) {
			return composable
		} else {
			throw IllegalStateException("ComposableScreenResolver was built on wrong ScreenRegistry")
		}
	}
}
