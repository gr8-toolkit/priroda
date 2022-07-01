package com.parimatch.navigation

import com.parimatch.navigation.reflection.Form
import com.parimatch.navigation.registry.ScreensRegistry
import com.parimatch.navigation.screen.Place
import com.parimatch.navigation.screen.Screen
import com.parimatch.navigation.screen.ScreenContext
import com.parimatch.navigation.state.State
import com.parimatch.navigation.state.Tab

internal fun ScreensRegistry.generateStateHavingScreensWithDifferentArgs() : State {
	val firstTab = Tab("first")
	val secondTab = Tab("second")
	val firstScreen = Screen(
		uid = "first",
		context = ScreenContext(Place.DEFAULT, null),
		description = resolveScreen(FirstScreen::class)!!,
		arg = Unit
	)
	val secondScreen = Screen(
		uid = "second",
		context = ScreenContext(Place.DEFAULT, null),
		description = resolveScreen(SecondScreen::class)!!,
		arg = ""
	)
	return State(
		backStackTabs = listOf(firstTab, secondTab),
		screens = mapOf(
			firstTab to listOf(firstScreen),
			secondTab to listOf(secondScreen)
		)
	)
}

internal fun ScreensRegistry.generateStateHavingScreensWithNonParcelableArgs() : State {
	val firstTab = Tab("first")
	val firstScreen = Screen(
		uid = "first",
		context = ScreenContext(Place.DEFAULT, null),
		description = resolveScreen(ThirdScreen::class)!!,
		arg = Form.Compose{}
	)
	return State(
		backStackTabs = listOf(firstTab),
		screens = mapOf(
			firstTab to listOf(firstScreen),
		)
	)
}

internal fun ScreensRegistry.generateStateHavingScreensAbsentInRegistry() : State {
	val firstTab = Tab("first")
	val firstScreen = Screen(
		uid = "first",
		context = ScreenContext(Place.DEFAULT, null),
		description = resolveScreen(FourthScreen::class)!!,
		arg = Unit
	)
	return State(
		backStackTabs = listOf(firstTab),
		screens = mapOf(
			firstTab to listOf(firstScreen),
		)
	)
}

internal fun ScreensRegistry.generateStateHavingSingleTab() : State {
	val firstTab = Tab("first")
	val firstScreen = Screen(
		uid = "first",
		context = ScreenContext(Place.DEFAULT, null),
		description = resolveScreen(FirstScreen::class)!!,
		arg = Unit
	)
	return State(
		backStackTabs = listOf(firstTab),
		screens = mapOf(
			firstTab to listOf(firstScreen),
		)
	)
}

internal fun ScreensRegistry.generateStateHavingTabsWithLotsOfScreens() : State {
	val firstTab = Tab("first")
	val firstScreen = Screen(
		uid = "first",
		context = ScreenContext(Place.DEFAULT, null),
		description = resolveScreen(FourthScreen::class)!!,
		arg = Unit
	)
	val secondScreen = Screen(
		uid = "second",
		context = ScreenContext(Place.DEFAULT, null),
		description = resolveScreen(SecondScreen::class)!!,
		arg = ""
	)
	val thirdScreen = Screen(
		uid = "third",
		context = ScreenContext(Place.DEFAULT, null),
		description = resolveScreen(SecondScreen::class)!!,
		arg = ""
	)
	val secondTab = Tab("second")
	val fourthScreen = Screen(
		uid = "fourth",
		context = ScreenContext(Place.DEFAULT, null),
		description = resolveScreen(FirstScreen::class)!!,
		arg = Unit
	)
	return State(
		backStackTabs = listOf(firstTab, secondTab),
		screens = mapOf(
			firstTab to listOf(firstScreen, secondScreen, thirdScreen),
			secondTab to listOf(fourthScreen)
		)
	)
}
