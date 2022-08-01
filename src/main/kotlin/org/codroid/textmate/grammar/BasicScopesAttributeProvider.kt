package org.codroid.textmate.grammar

import org.codroid.textmate.*
import org.codroid.textmate.theme.ScopeName

data class BasicScopeAttributes(val languageId: Int, val tokenType: OptionalStandardTokenType)

class BasicScopesAttributeProvider(initialLanguageId: Int, embeddedLanguages: EmbeddedLanguagesMap? = null) {

    private val defaultAttributes: BasicScopeAttributes
    private val embeddedLanguagesMatcher: ScopeMatcher<Int>

    companion object {
        private val NULL_SCOPE_METADATA = BasicScopeAttributes(0, 0)
        private val STANDARD_TOKEN_TYPE_REGEXP = Regex("\\b(comment|string|regex|meta\\.embedded)\\b")
    }

    init {
        defaultAttributes = BasicScopeAttributes(initialLanguageId, OptionalStandardTokenTypeConsts.NotSet)
        embeddedLanguagesMatcher = ScopeMatcher(embeddedLanguages)
    }

    fun getDefaultAttributes(): BasicScopeAttributes = defaultAttributes

    fun getBasicScopeAttributes(scopeName: ScopeName?): BasicScopeAttributes {
        if (scopeName == null) return NULL_SCOPE_METADATA
        return getBasicScopeAttributes.get(scopeName)
    }

    private val getBasicScopeAttributes = CachedFn<ScopeName, BasicScopeAttributes> {
        BasicScopeAttributes(scopeToLanguage(it), toStandardTokenType(it))
    }

    /**
     * Given a produced TM scope, return the language that token describes or null if unknown.
     * e.g. source.html => html, source.css.embedded.html => css, punctuation.definition.tag.html => null
     */
    private fun scopeToLanguage(scope: ScopeName): Int =
        embeddedLanguagesMatcher.match(scope) ?: 0

    private fun toStandardTokenType(scopeName: ScopeName): OptionalStandardTokenType {
        STANDARD_TOKEN_TYPE_REGEXP.findAll(scopeName).toList().getOrNull(0)?.let {
            return when (it.value) {
                "comment" -> OptionalStandardTokenTypeConsts.Comment
                "string" -> OptionalStandardTokenTypeConsts.String
                "regex" -> OptionalStandardTokenTypeConsts.RegEx
                "meta.embedded" -> OptionalStandardTokenTypeConsts.Other
                else -> throw IllegalArgumentException("Unexpected match for standard token type!")
            }
        }
        return OptionalStandardTokenTypeConsts.NotSet
    }
}

class ScopeMatcher<V>(private val values: Map<ScopeName, V>? = null) {
    private var scopesRegex: Regex? = null

    init {
        values?.let {
            // create the regex
            values.map { entry ->
                escapeRegExpCharacters(entry.key)
            }.sorted().reversed().let { scopes ->
                this.scopesRegex = Regex("^((${scopes.joinToString(")|(")}))($|\\.)")
            }
        }
    }

    fun match(scope: ScopeName): V? {
        scopesRegex?.let {
            val m = it.findAll(scope).toList()
            if (m.isNotEmpty()) {
                // matched
                return values!![m[1].value]
            }
        }
        return null
    }
}