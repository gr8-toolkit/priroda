package com.parimatch.navigation

import com.parimatch.navigation.reflection.Form
import com.parimatch.navigation.registry.ScreensModule
import com.parimatch.navigation.screen.BlankScreenContract
import com.parimatch.navigation.screen.ScreenContract

internal interface FirstScreen : BlankScreenContract

internal interface SecondScreen : ScreenContract<String, Unit>

internal interface ThirdScreen : ScreenContract<Form, Unit>

internal object FirstScreenModule : ScreensModule() {

	override val id: String get() = this::class.java.simpleName

	override fun Storage.fulfillScreens() {
		screen<FirstScreen>(
			clazz = FirstScreen::class,
			produceForm = { Form.Compose {} }
		)
		screen(
			clazz = SecondScreen::class,
			produceForm = { Form.Compose {} }
		)
		screen(
			clazz = ThirdScreen::class,
			produceForm = { Form.Compose {} }
		)
	}
}

internal interface FourthScreen : BlankScreenContract

internal object SecondScreenModule : ScreensModule() {

	override val id: String get() = this::class.java.simpleName

	override fun Storage.fulfillScreens() {
		screen<FourthScreen>(
			clazz = FourthScreen::class,
			produceForm = { Form.Compose {} }
		)
	}
}
