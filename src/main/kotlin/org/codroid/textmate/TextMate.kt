package org.codroid.textmate

import org.codroid.textmate.grammar.RawGrammar
import org.codroid.textmate.grammar.StateStack
import org.codroid.textmate.theme.RawTheme
import org.codroid.textmate.theme.ScopeName
import org.codroid.textmate.theme.Theme
import org.codroid.textmate.utils.OnigLib

/**
 * This file is equivalent to main.ts in vscode-textmate.
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/caab3de34a8cc7182141c9e31e0f42b96a3a1bac/src/main.ts">
 *     src/main.ts</a>
 */

typealias EmbeddedLanguagesMap = HashMap<ScopeName, Int>

interface RegistryOptions {
    val onigLib: OnigLib
    val theme: RawTheme?
    val colorMap: Array<String>

    suspend fun loadGrammar(scopeName: ScopeName): RawGrammar?

    fun getInjections(scopeName: ScopeName): Array<ScopeName>?
}


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
    val embeddedLanguages: EmbeddedLanguagesMap?,
    val tokenTypes: TokenTypeMap?,
    val balancedBracketSelectors: Array<String>,
    val unbalancedBracketSelectors: Array<String>
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

class Registry(val options: RegistryOptions) {
    private val syncRegistry: SyncRegistry = SyncRegistry(
        Theme.createFromRawTheme(options.theme, options.colorMap), options.onigLib
    )
}
