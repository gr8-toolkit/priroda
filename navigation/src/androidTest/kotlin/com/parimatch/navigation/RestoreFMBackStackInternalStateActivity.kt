package com.parimatch.navigation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit

internal class RestoreFMBackStackInternalStateActivity : AppCompatActivity() {

	private lateinit var fragmentContainerView: FragmentContainerView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(
			FragmentContainerView(this).apply {
				fragmentContainerView = this
				id = View.generateViewId()
			}
		)
	}

	internal fun makeFMHaveOnlyCurrentTab() {
		commitScreen("1", "first")
		commitScreen("2")
		supportFragmentManager.executePendingTransactions()
	}

	internal fun makeFMHaveFewTabs() {
		commitScreen("1", "first")
		commitScreen("2")
		supportFragmentManager.saveBackStack("first")
		commitScreen("3", "second")
		commitScreen("4")
		commitScreen("5")
		supportFragmentManager.executePendingTransactions()
	}

	internal fun makeFMHaveBackStackWithoutName() {
		commitScreen("1", "first")
		commitScreen("2")
		supportFragmentManager.saveBackStack("first")
		commitScreen("3")
		commitScreen("4")
		commitScreen("5")
		supportFragmentManager.executePendingTransactions()
	}

	internal fun makeFMHaveFragmentsWithoutTag() {
		commitScreen(null, "first")
		commitScreen("2")
		supportFragmentManager.saveBackStack("first")
		commitScreen("3", "second")
		commitScreen(null)
		commitScreen(null)
		supportFragmentManager.executePendingTransactions()
	}

	internal fun makeFMBeUsedBeforeReflection() {
		commitScreen(null, "first")
		supportFragmentManager.commit {
			setReorderingAllowed(true)
			addToBackStack(null)
			add(fragmentContainerView.id, EmptyFragment::class.java, bundleOf(), "2")
			add(fragmentContainerView.id, EmptyFragment::class.java, bundleOf(), "3")
		}
		supportFragmentManager.saveBackStack("first")
		commitScreen("4", "second")
		commitScreen(null)
		commitScreen(null)
		supportFragmentManager.executePendingTransactions()
	}

	private fun commitScreen(screen: String? = null, tab: String? = null) {
		supportFragmentManager.commit {
			setReorderingAllowed(true)
			addToBackStack(tab)
			add(fragmentContainerView.id, EmptyFragment::class.java, bundleOf(), screen)
		}
	}
}
