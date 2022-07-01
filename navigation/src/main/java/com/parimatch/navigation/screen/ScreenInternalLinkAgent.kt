package com.parimatch.navigation.screen

public data class ScreenInternalLinkAgent<A : Any>(
    public val aliases: Set<String>,
    public val argsTransformer: (Map<String, String>) -> A?
) {

    internal companion object {

        fun argumentless(aliases: Set<String>) : ScreenInternalLinkAgent<Unit> =
            ScreenInternalLinkAgent(aliases) {}
    }
}