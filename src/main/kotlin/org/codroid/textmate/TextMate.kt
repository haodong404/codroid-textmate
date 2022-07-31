package org.codroid.textmate

import org.codroid.textmate.grammar.BalancedBracketSelectors
import org.codroid.textmate.grammar.RawGrammar
import org.codroid.textmate.grammar.ScopeDependencyProcessor
import org.codroid.textmate.grammar.StateStack
import org.codroid.textmate.oniguruma.OnigLib
import org.codroid.textmate.theme.RawTheme
import org.codroid.textmate.theme.ScopeName
import org.codroid.textmate.theme.Theme

/**
 * This file is equivalent to main.ts in vscode-textmate.
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/caab3de34a8cc7182141c9e31e0f42b96a3a1bac/src/main.ts">
 *     src/main.ts</a>
 */

typealias EmbeddedLanguagesMap = HashMap<ScopeName, Int>

open class RegistryOptions(
    open val onigLib: OnigLib,
    val theme: RawTheme? = null,
    val colorMap: Array<String>? = null,
    val loadGrammar: (suspend (scopeName: ScopeName) -> RawGrammar?)? = null,
    val getInjections: ((scopeName: ScopeName) -> Array<ScopeName>?)? = null
)


/**
 * IGrammar
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/caab3de34a8cc7182141c9e31e0f42b96a3a1bac/src/main.ts#L197">
 *     src/main.ts#L197<a/>
 */
interface Tokenizer {
    /**
     * Tokenize `lineText` using previous line state `prevState`.
     */
    fun tokenizeLine(lineText: String, prevState: StateStack?, timeLimit: Int?): TokenizeLineResult

    /**
     * Tokenize `lineText` using previous line state `prevState`.
     * The result contains the tokens in binary format, resolved with the following information:
     *  - language
     *  - token type (regex, string, comment, other)
     *  - font style
     *  - foreground color
     *  - background color
     * e.g. for getting the languageId: `(metadata & MetadataConsts.LANGUAGEID_MASK) >>> MetadataConsts.LANGUAGEID_OFFSET`
     */
    fun tokenizeLine2(lineText: String, prevState: StateStack?, timeLimit: Int?): TokenizeLineResult2
}

data class GrammarConfiguration(
    val embeddedLanguages: EmbeddedLanguagesMap? = null,
    val tokenTypes: TokenTypeMap? = null,
    val balancedBracketSelectors: Array<String>? = null,
    val unbalancedBracketSelectors: Array<String>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GrammarConfiguration

        if (embeddedLanguages != other.embeddedLanguages) return false
        if (tokenTypes != other.tokenTypes) return false
        if (!balancedBracketSelectors.contentEquals(other.balancedBracketSelectors)) return false
        if (!unbalancedBracketSelectors.contentEquals(other.unbalancedBracketSelectors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = embeddedLanguages?.hashCode() ?: 0
        result = 31 * result + (tokenTypes?.hashCode() ?: 0)
        result = 31 * result + balancedBracketSelectors.contentHashCode()
        result = 31 * result + unbalancedBracketSelectors.contentHashCode()
        return result
    }
}

/**
 * The registry that will hold all grammars.
 */
class Registry(val options: RegistryOptions) {
    private val syncRegistry: SyncRegistry = SyncRegistry(
        Theme.createFromRawTheme(options.theme, options.colorMap), options.onigLib
    )

    private val ensureGrammarCache = HashMap<String, Boolean>()

    fun dispose() {
        this.syncRegistry.dispose()
    }

    /**
     * Change the theme. Once called, no previous `ruleStack` should be used anymore.
     */
    fun setTheme(theme: RawTheme, colorMap: Array<String>?) {
        this.syncRegistry.theme = Theme.createFromRawTheme(theme, colorMap)
    }

    /**
     * Returns a lookup array for color ids.
     */
    fun getColorMap(): Map<UInt, String> = this.syncRegistry.getColorMap()

    /**
     * Load the grammar for `scopeName` and all referenced included grammars asynchronously.
     * Please do not use language id 0.
     */
    suspend fun loadGrammarWithEmbeddedLanguages(
        initialScopeName: ScopeName, initialLanguage: Int, embeddedLanguages: EmbeddedLanguagesMap
    ): Tokenizer =
        this.loadGrammarWithConfiguration(initialScopeName, initialLanguage, GrammarConfiguration(embeddedLanguages))

    /**
     * Load the grammar for `scopeName` and all referenced included grammars asynchronously.
     * Please do not use language id 0.
     */
    suspend fun loadGrammarWithConfiguration(
        initialScopeName: ScopeName, initialLanguage: Int, configuration: GrammarConfiguration
    ): Tokenizer = this.loadGrammar(
        initialScopeName,
        initialLanguage,
        configuration.embeddedLanguages,
        configuration.tokenTypes,
        BalancedBracketSelectors(
            configuration.balancedBracketSelectors ?: emptyArray(),
            configuration.unbalancedBracketSelectors ?: emptyArray()
        )
    )

    /**
     * Load the grammar for `scopeName` and all referenced included grammars asynchronously.
     */
    suspend fun loadGrammar(initialScopeName: ScopeName): Tokenizer =
        this.loadGrammar(initialScopeName, 0, null, null, null)

    private suspend fun loadGrammar(
        initialScopeName: ScopeName,
        initialLanguage: Int,
        embeddedLanguages: EmbeddedLanguagesMap?,
        tokenTypes: TokenTypeMap?,
        balancedBracketSelectors: BalancedBracketSelectors?
    ): Tokenizer {
        val dependencyProcessor = ScopeDependencyProcessor(this.syncRegistry, initialScopeName)
        while (dependencyProcessor.qq.size > 0) {
            dependencyProcessor.qq.forEach {
                this.loadSingleGrammar(it.scopeName)
            }
            dependencyProcessor.processQueue()
        }
        return grammarForScopeName(
            initialScopeName,
            initialLanguage,
            embeddedLanguages,
            tokenTypes,
            balancedBracketSelectors
        )!!
    }

    private suspend fun loadSingleGrammar(scopeName: ScopeName): Boolean {
        if (!this.ensureGrammarCache.containsKey(scopeName)) {
            this.ensureGrammarCache[scopeName] = this.doLoadSingleGrammar(scopeName)
        }
        return this.ensureGrammarCache[scopeName] ?: false
    }

    private suspend fun doLoadSingleGrammar(scopeName: ScopeName): Boolean {
        val grammar = this.options.loadGrammar?.let { it(scopeName) }
        if (grammar != null) {
            val injections = if (this.options.getInjections != null) {
                this.options.getInjections.invoke(scopeName)
            } else {
                null
            }
            this.syncRegistry.addGrammar(grammar, injections)
            return true
        }
        return false
    }

    /**
     * Adds a rawGrammar.
     */
    suspend fun addGrammar(
        rawGrammar: RawGrammar,
        injections: Array<String> = emptyArray(),
        initialLanguage: Int = 0,
        embeddedLanguages: EmbeddedLanguagesMap? = null
    ): Tokenizer {
        this.syncRegistry.addGrammar(rawGrammar, injections)
        return this.grammarForScopeName(rawGrammar.scopeName, initialLanguage, embeddedLanguages)!!
    }

    /**
     * Get the grammar for `scopeName`. The grammar must first be created via `loadGrammar` or `addGrammar`.
     */
    private suspend fun grammarForScopeName(
        scopeName: String,
        initialLanguage: Int = 0,
        embeddedLanguages: EmbeddedLanguagesMap? = null,
        tokenTypes: TokenTypeMap? = null,
        balancedBracketSelectors: BalancedBracketSelectors? = null
    ): Tokenizer? {
        return this.syncRegistry.grammarForScopeName(
            scopeName, initialLanguage, embeddedLanguages, tokenTypes, balancedBracketSelectors
        )
    }

}
