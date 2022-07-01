package com.parimatch.navigation.reflection.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass
import androidx.compose.runtime.CompositionLocalProvider
import com.parimatch.navigation.reflection.LocalScreenUidOwner
import com.parimatch.navigation.reflection.ScreenUidOwner
import com.parimatch.navigation.callback.LocalNavigationCallbackRegistryOwner
import com.parimatch.navigation.callback.NavigationCallbackRegistry
import com.parimatch.navigation.callback.NavigationCallbackRegistryOwner

internal class ComposableFragment(
    private val callbackRegistry: NavigationCallbackRegistry,
	private val screenResolver: ComposableScreenResolver
) : Fragment() {

	@Suppress("UNCHECKED_CAST")
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return ComposeView(requireContext()).apply {
            val uid = requireArguments().getString(ARG_UID)!!
			val clazz = (requireArguments().get(ARG_CLAZZ) as Class<*>).kotlin
			val arg = requireArguments().get(ARG_ARG) ?: Unit
			setContent {
                CompositionLocalProvider(
                    LocalScreenUidOwner provides ScreenUidOwner { uid },
                    LocalNavigationCallbackRegistryOwner provides NavigationCallbackRegistryOwner { callbackRegistry }
                ) { screenResolver.get(clazz, arg)() }
			}
		}
	}

	companion object {
        private const val ARG_UID = "arg-uid"
		private const val ARG_CLAZZ = "arg-clazz"
		private const val ARG_ARG = "arg-arg"

		fun bundle(
            uid: String,
            clazz: KClass<*>,
            arg: Any
        ): Bundle {
			val argPromise = if (arg is Unit) null else arg
			return bundleOf(
                ARG_UID to uid,
				ARG_CLAZZ to clazz.java,
				ARG_ARG to argPromise
			)
		}
	}
}
