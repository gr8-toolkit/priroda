package com.parimatch.navigation.state

private const val CONTRACT_MESSAGE_1 = "Tab name cannot be empty String"

@JvmInline public value class Tab(public val value: String) {

	init {
		require(value != "") { CONTRACT_MESSAGE_1 }
	}
}
