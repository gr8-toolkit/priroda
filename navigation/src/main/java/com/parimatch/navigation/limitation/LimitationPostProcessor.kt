package com.parimatch.navigation.limitation

import com.parimatch.navigation.registry.ScreensRegistry
import com.parimatch.navigation.screen.NO_LIMITATION
import com.parimatch.navigation.state.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class LimitationPostProcessor(
    private val coroutineScope: CoroutineScope
) : PostProcessor {

	private val limitationJobs: MutableList<Pair<String, Job>> = mutableListOf()

    private var registry: ScreensRegistry? = null
        private set

    internal fun provideRegistry(screensRegistry: ScreensRegistry) {
        registry = screensRegistry
    }

	override fun invoke(old: State?, new: State, channel: SendChannel<Action>) {
        if (old == new) return

        new.currentTabScreens.screensDifference(limitationJobs.map { it.first }).forEach { op ->
			when (op) {
				Op.Pop -> {
					limitationJobs.removeAt(limitationJobs.size - 1).second.cancel()
				}
				is Op.Push -> {
					val job = coroutineScope.launch {
						op.screen.limitation
							.takeUnless { it == NO_LIMITATION }
							?.filterNot { it }
							?.map {}
							?.collect {
                                channel.send(
                                    registry?.popDownTo(op.screen)
                                        ?: throw IllegalStateException("ScreenRegistry was not set in LimitationPostProcessor")
                                )
                            }
					}
					limitationJobs.add(op.screen.uid to job)
				}
			}
		}
	}
}
