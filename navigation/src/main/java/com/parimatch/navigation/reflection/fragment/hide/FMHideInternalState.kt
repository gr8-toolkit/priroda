package com.parimatch.navigation.reflection.fragment.hide

import androidx.fragment.app.FragmentManager
import com.parimatch.navigation.reflection.fragment.NULL_SCREEN_UID
import com.parimatch.navigation.state.State
import com.parimatch.navigation.state.lastScreenInState

internal data class FMHideInternalState(
    val screens: Set<String>,
    val shown: List<String>
){

    companion object {

        fun equalFromState(state: State): FMHideInternalState =
            FMHideInternalState(
                state.screens.values.flatten().map { it.uid }.toSet(),
                listOf(state.currentTabScreens.lastScreenInState().uid)
            )

        /* temporarily restore only internal states */
        fun restoreFromFragmentManager(fm: FragmentManager): FMHideInternalState =
            FMHideInternalState(
                fm.fragments.map { it.tag ?: NULL_SCREEN_UID }.toSet(),
                fm.fragments.filter { !it.isHidden }.map { it.tag ?: NULL_SCREEN_UID }
            )
    }
}