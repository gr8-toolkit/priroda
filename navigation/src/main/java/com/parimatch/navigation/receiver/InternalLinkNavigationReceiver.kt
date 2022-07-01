package com.parimatch.navigation.receiver

import com.parimatch.navigation.state.Tab

public interface InternalLinkNavigationReceiver : NavigationReceiver {

	public fun replaceRoot(tab: Tab, internalLink: String)

	public fun openTab(tab: Tab, internalLink: String)

	public fun go(internalLink: String)
}
