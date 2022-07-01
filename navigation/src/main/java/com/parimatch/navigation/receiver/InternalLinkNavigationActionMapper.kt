package com.parimatch.navigation.receiver

import android.net.Uri
import com.parimatch.navigation.state.Action
import com.parimatch.navigation.state.Tab

internal class InternalLinkNavigationActionMapper(
	sendAction: (Action) -> Unit
) : NavigationActionMapper(sendAction), InternalLinkNavigationReceiver {

	override fun replaceRoot(tab: Tab, internalLink: String) {
		resolveTargetByInternalLink(internalLink)?.let { replaceRoot(tab, it) }
	}

	override fun openTab(tab: Tab, internalLink: String) {
		resolveTargetByInternalLink(internalLink)?.let { openTab(tab, it) }
	}

	override fun go(internalLink: String) {
		resolveTargetByInternalLink(internalLink)?.let { go(it) }
	}

	private fun resolveTargetByInternalLink(internalLink: String) : Target<*, *, *>? {
        val uri = Uri.parse(internalLink)
        val alias = uri.lastPathSegment ?: return null
        val description = registry?.resolveScreenByAlias(alias) ?: return null
        val agent = description.internalLinkAgent ?: return null
        val arg = agent.argsTransformer(uri.getQueryParameterMap()) ?: return null
        return Target::class.java
            .constructors[0]
            .newInstance(description.clazz, arg, null) as Target<*, *, *>
    }

    companion object {

        private fun Uri.getQueryParameterMap() : Map<String, String> =
            queryParameterNames.mapNotNull { name ->
                name to (getQueryParameter(name) ?: return@mapNotNull null)
            }.toMap()
    }
}
