package org.codroid.textmate.grammar

import org.codroid.textmate.*
import org.codroid.textmate.grammar.tokenize.tokenizeString
import org.codroid.textmate.regex.RegularExp
import org.codroid.textmate.regex.RegexLib
import org.codroid.textmate.regex.RegexScanner
import org.codroid.textmate.rule.*
import org.codroid.textmate.theme.ScopeName
import org.codroid.textmate.theme.ThemeProvider

fun createGrammar(
    scopeName: ScopeName,
    grammar: RawGrammar,
    initialLanguage: Int,
    embeddedLanguages: EmbeddedLanguagesMap?,
    tokenTypes: TokenTypeMap?,
    balancedBracketSelectors: BalancedBracketSelectors?,
    grammarRepository: GrammarReposThemeProvider,
    regexLib: RegexLib
): Grammar = Grammar(
    scopeName,
    grammar,
    initialLanguage,
    embeddedLanguages,
    tokenTypes,
    balancedBracketSelectors,
    grammarRepository,
    regexLib
)

interface GrammarReposThemeProvider : GrammarRepository, ThemeProvider, Cloneable

interface GrammarRepository {
    fun lookup(scopeName: ScopeName): RawGrammar?
    fun injections(targetScope: ScopeName): Array<ScopeName>?
}

fun initGrammar(grammar: RawGrammar, base: RawRule?): RawGrammar {
    val grammarClone = grammar.clone()
    grammarClone.repository.let {
        it["\$self"] = RawRule(
            patterns = grammarClone.patterns, name = grammarClone.scopeName
        )
        it["\$base"] = base ?: it["\$self"]!!
    }
    return grammarClone
}

class Grammar(
    private val rootScopeName: ScopeName,
    grammar: RawGrammar,
    initialLanguages: Int,
    embeddedLanguages: EmbeddedLanguagesMap?,
    tokenTypes: TokenTypeMap?,
    private val balancedBracketSelectors: BalancedBracketSelectors?,
    private val grammarRepository: GrammarReposThemeProvider,
    private val regexLib: RegexLib
) : Tokenizer, RuleFactoryHelper, RegexLib, RuleRegistryRegexLib, Cloneable {

    private var rootId = RuleId.End
    private var lastRuleId = RuleId.from(0)
    private val ruleId2Desc = mutableMapOf<Int, Rule?>()
    private val includedGrammars = mutableMapOf<ScopeName, RawGrammar>()
    private val grammar = initGrammar(grammar, null)
    private var injections: Array<Injection>? = null
    private val basicScopeAttributesProvider = BasicScopesAttributeProvider(
        initialLanguages, embeddedLanguages
    )
    private val tokenTypeMatchers = mutableListOf<TokenTypeMatcher>()

    init {
        if (tokenTypes != null) {
            for (selector in tokenTypes.keys) {
                Matchers.create(selector, ::nameMatcher).forEach { matcher ->
                    this.tokenTypeMatchers.add(
                        TokenTypeMatcher(
                            matcher = matcher.matcher, type = tokenTypes[selector]!!
                        )
                    )
                }
            }
        }
    }

    fun dispose() {
        for (rule in this.ruleId2Desc.values) {
            rule?.dispose()
        }
    }

    fun getMetadataForScope(scope: ScopeName): BasicScopeAttributes =
        this.basicScopeAttributesProvider.getBasicScopeAttributes(scope)

    private fun collectInjections(): Array<Injection> {
        val grammarRepository = object : GrammarRepository {
            override fun lookup(scopeName: ScopeName): RawGrammar? {
                if (scopeName == this@Grammar.rootScopeName) {
                    return this@Grammar.grammar
                }
                return this@Grammar.getExternalGrammar(scopeName, null)
            }

            override fun injections(targetScope: ScopeName): Array<ScopeName>? =
                this@Grammar.grammarRepository.injections(targetScope)
        }
        val result = mutableListOf<Injection>()
        val scopeName = this.rootScopeName
        grammarRepository.lookup(scopeName)?.let { grammar ->
            // add injections from the current grammar
            val rawInjections = grammar.injections
            if (rawInjections.isNotEmpty()) {
                for (expression in rawInjections.keys) {
                    collectInjections(
                        result, expression, rawInjections[expression]!!, this, grammar
                    )
                }
            }

            // add injection grammars contributed for the current scope

            this.grammarRepository.injections(scopeName)?.forEach {
                val injectionGrammar = this.getExternalGrammar(it, null)
                if (injectionGrammar != null) {
                    val selector = injectionGrammar.injectionSelector
                    if (selector != null) {
                        collectInjections(
                            result, selector, injectionGrammar.toRule(), this, injectionGrammar
                        )
                    }
                }
            }
        }
        result.sortWith { o1, o2 -> o1.priority - o2.priority }
        return result.toTypedArray()
    }

    fun getInjections(): Array<Injection> {
        if (this.injections == null) {
            this.injections = this.collectInjections()
            if (DebugFlag && this.injections?.isNotEmpty() == true) {
                println("Grammar ${this.rootScopeName} contains thie following injections:")
                for (injection in this.injections!!) {
                    println("  - ${injection.debugSelector}")
                }
            }
        }
        return this.injections!!
    }

    fun getThemeProvider(): ThemeProvider = this.grammarRepository

    override fun tokenizeLine(lineText: String, prevState: StateStack?, timeLimit: Int?): TokenizeLineResult {
        val result = this.tokenize(lineText, prevState, false, timeLimit ?: 0)
        return TokenizeLineResult(
            tokens = result.lineTokens.getResult(result.ruleStack, result.lineLength),
            ruleStack = result.ruleStack,
            stoppedEarly = result.stoppedEarly
        )
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun tokenizeLine2(lineText: String, prevState: StateStack?, timeLimit: Int?): TokenizeLineResult2 {
        val result = this.tokenize(lineText, prevState, true, timeLimit ?: 0)
        return TokenizeLineResult2(
            tokens = result.lineTokens.getBinaryResult(result.ruleStack, result.lineLength),
            ruleStack = result.ruleStack,
            stoppedEarly = result.stoppedEarly
        )
    }

    override fun getRule(ruleId: RuleId): Rule? = this.ruleId2Desc[ruleId.id]
    override fun <T : Rule> registerRule(factor: (id: RuleId) -> T): T {
        this.lastRuleId++
        val id = this.lastRuleId.clone()
        val result = factor(id)
        this.ruleId2Desc[id.id] = result
        return result
    }

    override fun getExternalGrammar(scopeName: ScopeName, repository: RawRepository?): RawGrammar? {
        if (this.includedGrammars[scopeName] != null) {
            return this.includedGrammars[scopeName]
        } else {
            this.grammarRepository.lookup(scopeName)?.let {
                val temp: RawRule? = if (repository != null) {
                    if (repository.containsKey("\$base")) {
                        repository["\$base"]
                    } else {
                        null
                    }
                } else {
                    null
                }
                this.includedGrammars[scopeName] = initGrammar(
                    it, temp
                )
                return this.includedGrammars[scopeName]
            }
            return null
        }
    }

    override fun createScanner(source: Array<String>): RegexScanner = this.regexLib.createScanner(source)

    override fun compile(pattern: String): RegularExp = this.regexLib.compile(pattern)

    data class TokenizeResult(
        val lineLength: Int,
        val lineTokens: LineTokens,
        val ruleStack: StateStack,
        val stoppedEarly: Boolean,
    )

    private fun tokenize(
        lineText: String, prevState: StateStack?, emitBinaryTokens: Boolean, timeLimit: Int
    ): TokenizeResult {
        if (this.rootId == RuleId.End) {
            this.rootId = RuleFactory.getCompiledRuleId(
                this.grammar.repository["\$self"]!!, this, this.grammar.repository
            )
        }

        val isFirstLine: Boolean
        val newPreState: StateStack
        if (prevState == null || prevState == StateStack.Null) {
            isFirstLine = true
            val rawDefaultMetadata = this.basicScopeAttributesProvider.getDefaultAttributes()
            val defaultStyle = this.getThemeProvider().getDefaults()
            val defaultMetadata = EncodedTokenAttributes.set(
                0u,
                rawDefaultMetadata.languageId.toUInt(),
                rawDefaultMetadata.tokenType,
                null,
                defaultStyle.fontStyle,
                defaultStyle.foregroundId.toUInt(),
                defaultStyle.backgroundId.toUInt()
            )
            val rootScopeName = this.getRule(this.rootId)?.getName(null, null)
            val scopeList: AttributedScopeStack = if (rootScopeName != null) {
                AttributedScopeStack.createRootAndLookUpScopeName(
                    rootScopeName, defaultMetadata, this
                )
            } else {
                AttributedScopeStack.createRoot("unknown", defaultMetadata)
            }
            newPreState = StateStack(null, this.rootId, -1, -1, false, null, scopeList, scopeList)
        } else {
            isFirstLine = false
            prevState.reset()
            newPreState = prevState
        }
        val newLineText = "$lineText\n"
        val lineTokens = LineTokens(
            emitBinaryTokens, newLineText, this.tokenTypeMatchers.toTypedArray(), this.balancedBracketSelectors
        )
        val result = tokenizeString(this, newLineText, isFirstLine, 0, newPreState, lineTokens, true, timeLimit)

        return TokenizeResult(newLineText.length, lineTokens, result.stack, result.stoppedEarly)
    }

}

fun nameMatcher(identifiers: List<ScopeName>, scopes: Array<ScopeName>): Boolean {
    if (scopes.size < identifiers.size) {
        return false
    }
    var lastIndex = 0
    return identifiers.every { identifier ->
        var i = lastIndex
        while (i < scopes.size) {
            if (scopesAreMatching(scopes[i], identifier)) {
                lastIndex = i + 1
                return@every true
            }
            i++
        }
        false
    }
}

fun scopesAreMatching(thisScopeName: String, scopeName: String): Boolean {
    if (thisScopeName.isEmpty()) return false
    if (thisScopeName == scopeName) return true
    val len = scopeName.length
    return thisScopeName.length > len && thisScopeName.substring(0, len) == scopeName && thisScopeName[len] == '.'
}

data class TokenTypeMatcher(val matcher: Matcher<Array<String>>, val type: StandardTokenType)