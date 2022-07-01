package com.parimatch.navigation.reflection.fragment.backstack

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.parimatch.navigation.reflection.fragment.FMReflection
import com.parimatch.navigation.reflection.fragment.NULL_SCREEN_UID
import com.parimatch.navigation.reflection.fragment.NULL_TAB
import com.parimatch.navigation.state.State
import com.parimatch.navigation.state.Tab
import java.lang.IllegalArgumentException

internal data class FMBackStackInternalState(
	val currentTab: Tab,
	val screens: Map<Tab, List<String>>
) {

	fun straightenOnCurrentTab(state: State): FMBackStackInternalState =
		FMBackStackInternalState(
			state.currentTab,
			screens.filter { state.screens.keys.contains(it.key) } +
					(state.currentTab to state.currentTabScreens.map { it.uid })
		)

	companion object {

		fun currentTabFromState(state: State): FMBackStackInternalState =
			FMBackStackInternalState(
				state.currentTab,
				mapOf(state.currentTab to state.currentTabScreens.map { it.uid })
			)

		fun equalFromState(state: State): FMBackStackInternalState =
			FMBackStackInternalState(
				state.currentTab,
				state.screens.map { entry -> entry.key to entry.value.map { it.uid } }.toMap()
			)

		/* temporarily restore only internal states */
		@Suppress("UNCHECKED_CAST")
		fun restoreFromFragmentManager(fm: FragmentManager): FMBackStackInternalState? {
			val screens = mutableMapOf<Tab, List<String>>()

			val backStackField = FragmentManager::class.java.getDeclaredField("mBackStack").apply { isAccessible = true }
			val nameField = FragmentTransaction::class.java.getDeclaredField("mName").apply { isAccessible = true }
			val opsField = FragmentTransaction::class.java.getDeclaredField("mOps").apply { isAccessible = true }
			val fragmentField =
				Class.forName("androidx.fragment.app.FragmentTransaction\$Op").getDeclaredField("mFragment").apply { isAccessible = true }

			val backStack = backStackField.get(fm)?.let { it as List<*> } ?: return null
			val currentTab = backStack.getOrNull(0)
				?.let {
					(nameField.get(it) as String?)
						?.let { name -> Tab(name) }
						?: NULL_TAB
				} ?: return null
			screens[currentTab] = backStack.flatMap {
				(opsField.get(it) as List<*>)
					.map { op -> fragmentField.get(op) as Fragment }
					.map { fragment -> fragment.tag ?: NULL_SCREEN_UID }
			}

			val fragmentStoreField = FragmentManager::class.java.getDeclaredField("mFragmentStore").apply { isAccessible = true }
			val savedStateField =
				Class.forName("androidx.fragment.app.FragmentStore").getDeclaredField("mSavedState").apply { isAccessible = true }
			val tagField = Class.forName("androidx.fragment.app.FragmentState").getDeclaredField("mTag").apply { isAccessible = true }
			val backStackStatesField = FragmentManager::class.java.getDeclaredField("mBackStackStates").apply { isAccessible = true }
			val transactionsField =
				Class.forName("androidx.fragment.app.BackStackState").getDeclaredField("mTransactions").apply { isAccessible = true }
			val backStackRecordStateField =
				Class.forName("androidx.fragment.app.BackStackRecordState").getDeclaredField("mFragmentWhos").apply { isAccessible = true }

			val savedState = savedStateField.get(fragmentStoreField.get(fm)) as Map<String, *>
			(backStackStatesField.get(fm) as Map<String, *>)
				.forEach { (name, stack) ->
					screens[Tab(name)] = (transactionsField.get(stack) as List<*>)
						.map { record ->
							(backStackRecordStateField.get(record) as List<String>)
								.also { if (it.size > 1) throw IllegalArgumentException(FMReflection.CONTRACT_EXCEPTION_MESSAGE_1) }
								.getOrNull(0) ?: throw IllegalArgumentException(FMReflection.CONTRACT_EXCEPTION_MESSAGE_1)
						}
						.map { who -> savedState[who] }
						.map { fragment ->
							tagField.get(fragment) as String? ?: NULL_SCREEN_UID
						}
				}

			return FMBackStackInternalState(currentTab, screens)
		}
	}
}
