package com.parimatch.navigation

import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.parimatch.navigation.reflection.fragment.NULL_SCREEN_UID
import com.parimatch.navigation.reflection.fragment.NULL_TAB
import com.parimatch.navigation.reflection.fragment.backstack.FMBackStackInternalState
import com.parimatch.navigation.state.Tab
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
internal class RestoreFMBackStackInternalStateTest {

	/* happy flow */

	@Test
	fun whenRestore_whileFMIsEmpty_shouldReturnNull() {
		launchActivity<RestoreFMBackStackInternalStateActivity>().use { scenario ->
			scenario.onActivity {
				val state = FMBackStackInternalState.restoreFromFragmentManager(it.supportFragmentManager)
				assertEquals(
					expected = null,
					actual = state
				)
			}
		}
	}

	@Test
	fun whenRestore_whileFMHasOnlyCurrentTab_shouldReturnStateWithOneTab() {
		launchActivity<RestoreFMBackStackInternalStateActivity>().use { scenario ->
			scenario.onActivity { activity ->
				activity.makeFMHaveOnlyCurrentTab()
				val first = Tab("first")
				val expected = FMBackStackInternalState(
					currentTab = first,
					screens = mapOf(first to listOf("1", "2"))
				)
				val state = FMBackStackInternalState.restoreFromFragmentManager(
					activity.supportFragmentManager
				)
				assertEquals(
					expected = expected,
					actual = state
				)
			}
		}
	}

	@Test
	fun whenRestore_whileFMHasFewTabs_shouldReturnStateWithFewTabsAndCorrectCurrentTab() {
		launchActivity<RestoreFMBackStackInternalStateActivity>().use { scenario ->
			scenario.onActivity { activity ->
				activity.makeFMHaveFewTabs()
				val first = Tab("first")
				val second = Tab("second")
				val expected = FMBackStackInternalState(
					currentTab = second,
					screens = mapOf(
						first to listOf("1", "2"),
						second to listOf("3", "4", "5")
					)
				)
				val state = FMBackStackInternalState.restoreFromFragmentManager(
					activity.supportFragmentManager
				)
				assertEquals(
					expected = expected,
					actual = state
				)
			}
		}
	}

	/* unhappy flow */

	@Test
	fun whenRestore_whileFMHasBackStackWithoutName_shouldReturnCurrentTabAsNullTab() {
		launchActivity<RestoreFMBackStackInternalStateActivity>().use { scenario ->
			scenario.onActivity { activity ->
				activity.makeFMHaveBackStackWithoutName()
				val first = Tab("first")
				val second = NULL_TAB
				val expected = FMBackStackInternalState(
					currentTab = second,
					screens = mapOf(
						first to listOf("1", "2"),
						second to listOf("3", "4", "5")
					)
				)
				val state = FMBackStackInternalState.restoreFromFragmentManager(
					activity.supportFragmentManager
				)
				assertEquals(
					expected = expected,
					actual = state
				)
			}
		}
	}

	@Test
	fun whenRestore_whileFMHasFragmentsWithoutTag_shouldReturnScreenWithNullUid() {
		launchActivity<RestoreFMBackStackInternalStateActivity>().use { scenario ->
			scenario.onActivity { activity ->
				activity.makeFMHaveFragmentsWithoutTag()
				val first = Tab("first")
				val second = Tab("second")
				val expected = FMBackStackInternalState(
					currentTab = second,
					screens = mapOf(
						first to listOf(NULL_SCREEN_UID, "2"),
						second to listOf("3", NULL_SCREEN_UID, NULL_SCREEN_UID)
					)
				)
				val state = FMBackStackInternalState.restoreFromFragmentManager(
					activity.supportFragmentManager
				)
				assertEquals(
					expected = expected,
					actual = state
				)
			}
		}
	}

	@Test
	fun whenRestore_whileFMHasBeenUsedBeforeReflection_shouldThrowIllegalArgumentException() {
		launchActivity<RestoreFMBackStackInternalStateActivity>().use { scenario ->
			scenario.onActivity { activity ->
				activity.makeFMBeUsedBeforeReflection()
				assertFailsWith<IllegalArgumentException> {
                    FMBackStackInternalState.restoreFromFragmentManager(
						activity.supportFragmentManager
					)
				}
			}
		}
	}
}
