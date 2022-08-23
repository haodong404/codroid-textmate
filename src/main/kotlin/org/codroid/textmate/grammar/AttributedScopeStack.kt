package org.codroid.textmate.grammar

import org.codroid.textmate.EncodedToken
import org.codroid.textmate.EncodedTokenAttributes
import org.codroid.textmate.theme.*

class AttributedScopeStack(
    private val parent: AttributedScopeStack?,
    private val scopePath: ScopeStack,
    val tokenAttributes: EncodedToken
) {
    companion object {
        fun createRoot(scopeName: ScopeName, tokenAttributes: EncodedToken): AttributedScopeStack =
            AttributedScopeStack(null, ScopeStack(null, scopeName), tokenAttributes)

        fun createRootAndLookUpScopeName(
            scopeName: ScopeName,
            tokenAttributes: EncodedToken,
            grammar: Grammar
        ): AttributedScopeStack {
            val rawRootMetadata = grammar.getMetadataForScope(scopeName)
            val scopePath = ScopeStack(null, scopeName)
            val rootStyle = grammar.getThemeProvider().themeMatch(scopePath)
            val resolvedTokenAttributes = mergeAttributes(tokenAttributes, rawRootMetadata, rootStyle)
            return AttributedScopeStack(null, scopePath, resolvedTokenAttributes)
        }

        fun mergeAttributes(
            existingTokenAttributes: EncodedToken,
            basicScopeAttributes: BasicScopeAttributes,
            styleAttributes: StyleAttributes?
        ): EncodedToken {
            var fontStyle = FontStyleConsts.NotSet
            var foreground = 0
            var background = 0

            if (styleAttributes != null) {
                fontStyle = styleAttributes.fontStyle
                foreground = styleAttributes.foregroundId
                background = styleAttributes.backgroundId
            }
            return EncodedTokenAttributes.set(
                existingTokenAttributes,
                basicScopeAttributes.languageId.toUInt(),
                basicScopeAttributes.tokenType,
                null, fontStyle, foreground.toUInt(), background.toUInt()
            )
        }

    }

    fun getScopeName(): ScopeName = this.scopePath.scopeName

    fun pushAttributed(scopePath: ScopePath?, grammar: Grammar): AttributedScopeStack {
        if (scopePath == null) return this
        if (scopePath.indexOf(' ') == -1) {
            // This is the common case and much faster
            return pushAttributed(this, scopePath, grammar)
        }
        val scopes = scopePath.split(' ')
        var result = this
        for (scope in scopes) {
            result = pushAttributed(result, scope, grammar)
        }
        return result
    }

    private fun pushAttributed(
        target: AttributedScopeStack,
        scopeName: ScopeName, grammar: Grammar
    ): AttributedScopeStack {
        val rawMetadata = grammar.getMetadataForScope(scopeName)
        val newPath = target.scopePath.push(scopeName)
        val scopeThemeMatchResult = grammar.getThemeProvider().themeMatch(newPath)
        val metadata = mergeAttributes(
            target.tokenAttributes,
            rawMetadata, scopeThemeMatchResult
        )
        return AttributedScopeStack(target, newPath, metadata)
    }

    fun getScopeNames(): Array<String> = this.scopePath.getSegments().toTypedArray()

    private fun equals(first: AttributedScopeStack?, second: AttributedScopeStack?): Boolean {
        var a = first
        var b = second
        do {
            if (a === b) {
                return true
            }
            if (a == null && b == null) {
                // End of list reached for both.
                return true
            }

            if (a == null || b == null) {
                // End of list reached only for one
                return false
            }
            if (a.getScopeName() != b.getScopeName() || a.tokenAttributes != b.tokenAttributes) {
                return false
            }
            // Go to previous pair
            a = a.parent
            b = b.parent
        } while (true)

    }

    override fun equals(other: Any?): Boolean {
        return if (other is AttributedScopeStack) {
            equals(this, other)
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = parent?.hashCode() ?: 0
        result = 31 * result + scopePath.hashCode()
        result = 31 * result + tokenAttributes.hashCode()
        return result
    }

}