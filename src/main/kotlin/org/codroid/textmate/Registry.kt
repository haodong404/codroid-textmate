package org.codroid.textmate

import org.codroid.textmate.grammar.*
import org.codroid.textmate.theme.ScopeName
import org.codroid.textmate.theme.ScopeStack
import org.codroid.textmate.theme.StyleAttributes
import org.codroid.textmate.theme.Theme
import org.codroid.textmate.utils.OnigLib

/**
 * SyncRegistry
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/caab3de34a8cc7182141c9e31e0f42b96a3a1bac/src/registry.ts#L11">
 *     src/registry.ts#L11</a>
 */
class SyncRegistry(
    var theme: Theme, val onigLib: OnigLib
) : GrammarReposThemeProvider {

    private val grammars = mutableMapOf<ScopeName, Grammar>()
    private val rawGrammars = mutableMapOf<ScopeName, RawGrammar>()
    private val injectionGrammars = mutableMapOf<ScopeName, Array<ScopeName>>()

    fun dispose() {
        for (grammar in this.grammars.values) {
            grammar.dispose()
        }
    }

    fun getColorMap(): Map<UInt, String> = this.theme.getColorMap()

    /**
     * Add `grammar` to registry and return a list of referenced scope names
     */
    fun addGrammar(grammar: RawGrammar, injectionScopeName: Array<ScopeName>?) {
        TODO()
    }

    /**
     * Lookup a raw grammar.
     */
    override fun lookup(scopeName: ScopeName): RawGrammar? = this.rawGrammars[scopeName]

    /**
     * Returns the injections for the given grammar
     */
    override fun injections(targetScope: ScopeName): Array<ScopeName> = this.injectionGrammars[targetScope]!!

    /**
     * Match a scope in the theme.
     */
    override fun themeMatch(scopePath: ScopeStack): StyleAttributes? = this.theme.match(scopePath)


    override fun getDefaults(): StyleAttributes = this.theme.getDefaults()

    /**
     * Lookup a grammar
     */
    suspend fun grammarForScopeName(
        scopeName: ScopeName,
        initialLanguage: Int,
        embeddedLanguages: EmbeddedLanguagesMap?,
        tokenTypes: TokenTypeMap?,
        balancedBracketSelectors: BalancedBracketSelectors?
    ): Tokenizer? {
        if (!this.grammars.contains(scopeName)) {
            val rawGrammar = this.rawGrammars[scopeName] ?: return null
            this.grammars[scopeName] = createGrammar(
                scopeName, rawGrammar, initialLanguage, embeddedLanguages, tokenTypes, balancedBracketSelectors, this,
                this.onigLib
            )
        }
        return this.grammars[scopeName]
    }
}