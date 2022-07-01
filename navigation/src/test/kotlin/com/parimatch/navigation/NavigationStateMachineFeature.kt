package com.parimatch.navigation

import com.parimatch.navigation.Navigation.Companion.generateInitialState
import com.parimatch.navigation.receiver.*
import com.parimatch.navigation.state.NavigationStateMachine
import com.parimatch.navigation.state.State
import com.parimatch.navigation.state.Tab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal object NavigationStateMachineFeature :
	Spek({
			 beforeGroup { Dispatchers.setMain(TestCoroutineDispatcher()) }
			 afterGroup { Dispatchers.resetMain() }
			 val navigationRegistry by memoized (CachingMode.SCOPE) {
				 Navigation("test").setup(false).registry.apply {
					 register(FirstScreenModule, SecondScreenModule)
				 }
			 }
			 val inadequateNavigationRegistry by memoized (CachingMode.SCOPE) {
				 Navigation("test").setup(false).registry.apply {
					 register(FirstScreenModule)
				 }
			 }

			 Feature("NavigationStateMachine") {
				 val stateMachine by memoized {
					 val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
					 NavigationStateMachine(scope)
				 }

				 Scenario("adding an action before calling NavigationStateMachine::handleActions") {
					 Given("a machine with single pending action") {
						 stateMachine.sendAction(
							 navigationRegistry.replaceRootTab(
								 Tab("first"),
                                 Target(FirstScreen::class, Unit)
							 )
						 )
					 }
					 var state: State? = null
					 When("getting the state") {
						 runBlockingTest {
							 state = stateMachine.handleActions(navigationRegistry.generateInitialState())
								 .take(1)
								 .toList()
								 .firstOrNull()
						 }
					 }
					 Then("it should return state having single tab") {
						 assert(state?.screens?.size == 1)
					 }
					 And("it should return state having right tab") {
						 val expected = navigationRegistry.generateStateHavingSingleTab()
						 assertEquals(
							 expected = expected.currentTab,
							 actual = state?.currentTab
						 )
					 }
				 }

				 Scenario("adding an action containing an unregistered screen") {
					 Given("a machine with single pending action") {
						 stateMachine.sendAction(
							 inadequateNavigationRegistry.replaceRootTab(
								 Tab("first"),
                                 Target(FourthScreen::class, Unit)
							 )
						 )
					 }
					 lateinit var initialState: State
					 var state: State? = null
					 When("getting the state") {
						 initialState = navigationRegistry.generateInitialState()
						 runBlockingTest {
							 state = stateMachine.handleActions(initialState)
								 .take(1)
								 .toList()
								 .firstOrNull()
						 }
					 }
					 Then("it should return state having an initial state") {
						 assertEquals(
							 expected = initialState,
							 actual = state
						 )
					 }
				 }

				 Scenario("adding lots of actions") {
					 lateinit var flowOfStates: StateFlow<State?>
					 Given("a machine having lots of pending actions"){
						 flowOfStates = stateMachine.handleActions(navigationRegistry.generateInitialState())
						 stateMachine.sendAction(navigationRegistry.replaceRootTab(Tab("first"),
                             Target(FourthScreen::class, Unit)
                         ))
						 stateMachine.sendAction(navigationRegistry.push(Target(SecondScreen::class, "")))
						 stateMachine.sendAction(navigationRegistry.push(Target(SecondScreen::class, "")))
						 stateMachine.sendAction(navigationRegistry.pushTab(Tab("second"),
                             Target(FirstScreen::class, Unit)
                         ))
						 stateMachine.sendAction(navigationRegistry.push(Target(SecondScreen::class, "")))
						 stateMachine.sendAction(navigationRegistry.pop())
					 }
					 var state: State? = null
					 When("getting the state") {
						 runBlockingTest {
							 state = flowOfStates
								 .take(1)
								 .toList()
								 .firstOrNull()
						 }
					 }
					 lateinit var expected: State
					 Then("it should return state having a correct state") {
						 expected = navigationRegistry.generateStateHavingTabsWithLotsOfScreens()
						 assertEquals(
							 expected = expected.screens.mapValues { entry ->
								 entry.value.map { it.description }
							 },
							 actual = state?.screens?.mapValues { entry ->
							 	entry.value.map { it.description }
							 }
						 )
					 }
					 And("it should return state having a correct back stack") {
						 assertEquals(
							 expected = expected.backStackTabs,
							 actual = state?.backStackTabs
						 )
					 }
				 }
			 }
		 })
