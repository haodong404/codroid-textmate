package org.codroid.textmate.rule

import org.codroid.textmate.grammar.Location
import org.codroid.textmate.utils.OnigCaptureIndex

class BeginWhileRule(
    override val location: Location?,
    override val id: RuleId,
    override val name: String?,
    override val contentName: String?,

    begin: String,
    val beginCaptures: Array<CaptureRule?>,
    _while: String,
    val whileCaptures: Array<CaptureRule?>,
    patterns: CompilePatternsResult
) : Rule(location, id, name, contentName), WithPatternRule {
    private val begin = RegExpSource(begin, this.id)
    private val _while = RegExpSource(_while, RuleId.While)
    private val whileHasBackReferences = this._while.hasBackReferences
    override val patterns = patterns.patterns
    override val hasMissingPatterns = patterns.hasMissingPatterns
    private var cachedCompiledPatterns: RegExpSourceList? = null
    private var cachedCompiledWhilePatterns: RegExpSourceList? = null
    override fun dispose() {
        if (this.cachedCompiledPatterns != null) {
            this.cachedCompiledPatterns!!.dispose()
            this.cachedCompiledPatterns = null
        }
        if (this.cachedCompiledWhilePatterns != null) {
            this.cachedCompiledWhilePatterns!!.dispose()
            this.cachedCompiledWhilePatterns = null
        }
    }

    fun debugBeginRegExp(): String = this.begin.source

    fun debugWhileRegExp(): String = this._while.source

    fun getWhileWithResolvedBackReferences(lineText: String, captureIndices: Array<OnigCaptureIndex>): String =
        this._while.resolveBackReferences(lineText, captureIndices)

    override fun collectPatterns(grammar: RuleRegistry, out: RegExpSourceList) {
        out.push(this.begin)
    }

    override fun compile(grammar: RuleRegistryOnigLib, endRegexSource: String): CompiledRule =
        this.getCachedCompiledPatterns(grammar).compile(grammar)

    override fun compileAG(
        grammar: RuleRegistryOnigLib,
        endRegexSource: String,
        allowA: Boolean,
        allowG: Boolean
    ): CompiledRule = this.getCachedCompiledPatterns(grammar).compileAG(grammar, allowA, allowG)

    private fun getCachedCompiledPatterns(grammar: RuleRegistryOnigLib): RegExpSourceList {
        if (this.cachedCompiledPatterns == null) {
            this.cachedCompiledPatterns = RegExpSourceList()
            for (pattern in this.patterns) {
                grammar.getRule(pattern).collectPatterns(grammar, this.cachedCompiledPatterns!!)
            }
        }
        return this.cachedCompiledPatterns!!
    }

    fun compileWhile(grammar: RuleRegistryOnigLib, endRegexSource: String?): CompiledRule =
        this.getCachedCompiledWhilePatterns(grammar, endRegexSource).compile(grammar)

    fun compileWhileAG(
        grammar: RuleRegistryOnigLib,
        endRegexSource: String?,
        allowA: Boolean,
        allowG: Boolean
    ): CompiledRule = this.getCachedCompiledWhilePatterns(grammar, endRegexSource).compileAG(grammar, allowA, allowG)

    private fun getCachedCompiledWhilePatterns(
        grammar: RuleRegistryOnigLib,
        endRegexSource: String?
    ): RegExpSourceList {
        if (this.cachedCompiledWhilePatterns == null) {
            this.cachedCompiledWhilePatterns = RegExpSourceList()
            this.cachedCompiledWhilePatterns!!.push(if (this._while.hasBackReferences) this._while.clone() else this._while)
        }
        if (this._while.hasBackReferences) {
            this.cachedCompiledWhilePatterns!!.setSource(0, endRegexSource ?: "\uFFFF")
        }
        return this.cachedCompiledWhilePatterns!!
    }

}