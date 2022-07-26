package org.codroid.textmate.grammar

import org.codroid.textmate.*
import org.codroid.textmate.theme.ScopeName
import org.codroid.textmate.theme.ThemeProvider
import org.codroid.textmate.utils.OnigLib

fun createGrammar(
    scopeName: ScopeName,
    grammar: RawGrammar,
    initialLanguage: Int,
    embeddedLanguages: EmbeddedLanguagesMap?,
    tokenTypes: TokenTypeMap?,
    balancedBracketSelectors: BalancedBracketSelectors?,
    grammarRepository: GrammarReposThemeProvider,
    onigLib: OnigLib
): Grammar {
    TODO()
}

interface GrammarReposThemeProvider : GrammarRepository, ThemeProvider

interface GrammarRepository {
    fun lookup(scopeName: ScopeName): RawGrammar?
    fun injections(targetScope: ScopeName): Array<ScopeName>
}

class Grammar : Tokenizer {

    fun dispose() {
        TODO()
    }

    override fun tokenizeLine(lineText: String, prevState: StateStack?, timeLimit: Int?): TokenizeLineResult {
        TODO("Not yet implemented")
    }

    override fun tokenizeLine2(lineText: String, prevState: StateStack?, timeLimit: Int?): TokenizeLineResult2 {
        TODO("Not yet implemented")
    }

}
