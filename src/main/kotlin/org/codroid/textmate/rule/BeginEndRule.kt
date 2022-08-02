package org.codroid.textmate.rule

import org.codroid.textmate.RegexSource

class BeginEndRule(
    override val id: RuleId,
    override val name: String?,
    override val contentName: String?,

    begin: String,
    val beginCaptures: Array<CaptureRule?>,
    end: String? = null,
    val endCaptures: Array<CaptureRule?>,
    val applyEndPatternLast: Boolean = false,
    patterns: CompilePatternsResult
) : Rule(), WithPatternRule {
    override val nameIsCapturing: Boolean = RegexSource.hasCaptures(name)
    override val contentNameIsCapturing: Boolean = RegexSource.hasCaptures(contentName)

    private val begin: RegExpSource = RegExpSource(begin, this.id)
    private val end: RegExpSource = RegExpSource(end ?: "\uFFFF", RuleId.End)
    val endHasBackReferences: Boolean = this.end.hasBackReferences
    override val patterns: Array<RuleId> = patterns.patterns
    override val hasMissingPatterns: Boolean = patterns.hasMissingPatterns
    private var cachedCompiledPatterns: RegExpSourceList? = null
    override fun dispose() {
        if (this.cachedCompiledPatterns != null) {
            this.cachedCompiledPatterns!!.dispose()
            this.cachedCompiledPatterns = null
        }
    }

    fun debugBeginRegExp(): String = this.begin.source

    fun debugEndRegExp(): String = this.end.source

    fun getEndWithResolvedBackReferences(lineText: String, captureIndices: Array<IntRange>): String =
        this.end.resolveBackReferences(lineText, captureIndices)

    override fun collectPatterns(grammar: RuleRegistry, out: RegExpSourceList) {
        out.push(this.begin)
    }

    override fun compile(grammar: RuleRegistryRegexLib, endRegexSource: String): CompiledRule =
        this.getCachedCompiledPatterns(grammar, endRegexSource ?: "").compile(grammar)

    override fun compileAG(
        grammar: RuleRegistryRegexLib,
        endRegexSource: String,
        allowA: Boolean,
        allowG: Boolean
    ): CompiledRule = this.getCachedCompiledPatterns(grammar, endRegexSource).compileAG(grammar, allowA, allowG)

    private fun getCachedCompiledPatterns(grammar: RuleRegistryRegexLib, endRegexSource: String): RegExpSourceList {
        if (this.cachedCompiledPatterns == null) {
            this.cachedCompiledPatterns = RegExpSourceList()
            for (pattern in this.patterns) {
                grammar.getRule(pattern)?.collectPatterns(grammar, this.cachedCompiledPatterns!!)
            }
            val temp = if (this.end.hasBackReferences) {
                this.end.clone()
            } else {
                this.end
            }
            if (this.applyEndPatternLast) {
                this.cachedCompiledPatterns!!.push(temp)
            } else {
                this.cachedCompiledPatterns!!.unshift(temp)
            }
        }
        if (this.end.hasBackReferences) {
            if (this.applyEndPatternLast) {
                this.cachedCompiledPatterns!!.setSource(this.cachedCompiledPatterns!!.length() - 1, endRegexSource)
            } else {
                this.cachedCompiledPatterns!!.setSource(0, endRegexSource)
            }
        }
        return this.cachedCompiledPatterns!!
    }
}