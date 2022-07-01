package com.parimatch.navigation.state

internal fun interface Action {
	fun act(state: State): State?
}
