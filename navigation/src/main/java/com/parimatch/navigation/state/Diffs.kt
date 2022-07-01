package com.parimatch.navigation.state

import com.parimatch.navigation.screen.Screen

internal sealed class Op {

	data class Push(val screen: Screen<out Any>) : Op()

	object Pop : Op()
}

internal fun List<Screen<out Any>>.handleScreensDifference(screens: List<String>)
		: (() -> Unit) -> ((Screen<out Any>) -> Unit) -> Unit =
	{ pop ->
		{ push ->
			screensDifference(screens).forEach { op ->
				when (op) {
					Op.Pop -> pop()
					is Op.Push -> push(op.screen)
				}
			}
		}
	}

internal fun List<Screen<out Any>>.screensDifference(screens: List<Screen<out Any>>): List<Op> {
	return screensDifference(screens.map { it.uid })
}

@JvmName("screenUidsDifference")
internal fun List<Screen<out Any>>.screensDifference(screens: List<String>): List<Op> =
	mutableListOf<Op>().also { ops ->
		var restored = 0
		for (i in 0 until size.coerceAtMost(screens.size)) {
			if (get(i).uid != screens[i]) break
			else restored++
		}

		repeat(screens.size - restored) { ops.add(Op.Pop) }

		takeLast(size - restored).forEach { ops.add(Op.Push(it)) }
	}
