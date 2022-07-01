package com.parimatch.navigation

import android.os.Bundle
import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.parimatch.navigation.reflection.StateBundler
import com.parimatch.navigation.registry.ScreensRegistry
import com.parimatch.navigation.state.State
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
internal class StateBundlerTest {

	private lateinit var stateBundler: StateBundler
	private lateinit var registry: ScreensRegistry

	@Before fun setup() {
		val interfaces = Navigation("test").setup(false)
		interfaces.registry.register(FirstScreenModule, SecondScreenModule)
		stateBundler = StateBundler(interfaces.registry)
		registry = interfaces.registry
	}

	/* happy flow */

	@Test
	fun whenParse_whileStateIsEmpty_shouldReturnNull() {
		val parcel = Parcel.obtain()
		val bundle: Bundle = Bundle.CREATOR.createFromParcel(parcel)
		val state: State? = stateBundler.fromBundle(bundle)
		assertEquals(
			expected = null,
			actual = state
		)
	}

	@Test
	fun whenParse_whileStateHasScreensWithDifferentArgs_shouldReturnEqualState() {
		val expected = registry.generateStateHavingScreensWithDifferentArgs()
		val parcel = Parcel.obtain()
		parcel.writeBundle(stateBundler.toBundle(expected))
		parcel.setDataPosition(0)
		val bundle: Bundle = Bundle.CREATOR.createFromParcel(parcel)
		val state: State? = stateBundler.fromBundle(bundle)
		assertEquals(
			expected = expected,
			actual = state
		)
	}

	/* unhappy flow */

	@Test
	fun whenParse_whileStateHasScreensWithNonParcelableArgs_shouldThrowIllegalArgumentException() {
		assertFailsWith<IllegalArgumentException> {
			val state = registry.generateStateHavingScreensWithNonParcelableArgs()
			stateBundler.toBundle(state)
		}
	}

	@Test
	fun whenParse_whileStateHasScreensAbsentInRegistry_shouldThrowIllegalStateException() {
		assertFailsWith<IllegalStateException> {
			val state = registry.generateStateHavingScreensAbsentInRegistry()
			val parcel = Parcel.obtain()
			parcel.writeBundle(stateBundler.toBundle(state))
			parcel.setDataPosition(0)
			val bundle: Bundle = Bundle.CREATOR.createFromParcel(parcel)
			val interfaces = Navigation("test-2").setup(false)
			interfaces.registry.register(FirstScreenModule)
			StateBundler(interfaces.registry).fromBundle(bundle)
		}
	}
}
