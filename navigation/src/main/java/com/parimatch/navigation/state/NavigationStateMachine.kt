package com.parimatch.navigation.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

internal typealias Intermediary = (action: Action, state: State, channel: SendChannel<Action>) -> Unit

internal typealias PostProcessor = (old: State?, new: State, channel: SendChannel<Action>) -> Unit

internal class NavigationStateMachine(
	private val coroutineScope: CoroutineScope,
	private val intermediaries: List<Intermediary> = emptyList(),
	private val postProcessors: List<PostProcessor> = emptyList()
) {

    companion object {
        internal const val CONTRACT_EXCEPTION_MESSAGE_1 = "Null state have to be the last one"
    }

	private var _state: MutableStateFlow<State?>? = null
	private val actions = Channel<Action>() // todo optimise

	fun sendAction(action: Action) {
		coroutineScope.launch { actions.send(action) }
	}

	internal fun handleActions(initial: State): StateFlow<State?> {
		return _state ?: let {
			MutableStateFlow<State?>(initial).also { state ->
				coroutineScope.launch {
					postProcess(null, initial)
                    var isReceiving = true
					for (action in actions) {
						if (!(isActive && isReceiving)) break

                        state.value
                            ?.let { old ->
                                mediation(action, old) // Actor

                                val new = action.act(old) // Reducer
                                state.value = new

                                new?.let { postProcess(old, new) } // PostProcessor
                                    ?: run { isReceiving = false }
                            } ?: throw IllegalStateException(CONTRACT_EXCEPTION_MESSAGE_1)
					}
				}
				_state = state
			}
		}
	}

	private fun mediation(action: Action, state: State) {
		intermediaries.forEach { intermediary -> intermediary(action, state, actions) }
	}

    private fun postProcess(old: State?, new: State) {
        postProcessors.forEach { processor -> processor(old, new, actions) }
    }
}
