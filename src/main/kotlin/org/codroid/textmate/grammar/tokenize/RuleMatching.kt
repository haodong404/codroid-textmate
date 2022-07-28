package org.codroid.textmate.grammar.tokenize

import org.codroid.textmate.grammar.Grammar
import org.codroid.textmate.grammar.Injection
import org.codroid.textmate.grammar.LineTokens
import org.codroid.textmate.grammar.StateStack
import org.codroid.textmate.rule.*
import org.codroid.textmate.utils.OnigCaptureIndex
import org.codroid.textmate.utils.OnigString

data class MatchResult(val captureIndices: Array<OnigCaptureIndex>, val matchedRuleId: RuleId) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MatchResult

        if (!captureIndices.contentEquals(other.captureIndices)) return false
        if (matchedRuleId != other.matchedRuleId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = captureIndices.contentHashCode()
        result = 31 * result + matchedRuleId.hashCode()
        return result
    }
}

data class MatchInjectionsResult(
    val priorityMatch: Boolean,
    val captureIndices: Array<OnigCaptureIndex>,
    val matchedRuled: RuleId
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MatchInjectionsResult

        if (priorityMatch != other.priorityMatch) return false
        if (!captureIndices.contentEquals(other.captureIndices)) return false
        if (matchedRuled != other.matchedRuled) return false

        return true
    }

    override fun hashCode(): Int {
        var result = priorityMatch.hashCode()
        result = 31 * result + captureIndices.contentHashCode()
        result = 31 * result + matchedRuled.hashCode()
        return result
    }
}

data class PrepareRuleResult(val ruleScanner: CompiledRule, val findOptions: Int)

object RuleMatching {
    fun matchRuleOrInjections(
        grammar: Grammar,
        lineText: OnigString,
        isFirstLine: Boolean,
        linePos: Int,
        stack: StateStack,
        anchorPosition: Int
    ): MatchResult? {
        TODO()
    }

    fun matchRule(
        grammar: Grammar,
        lineText: OnigString,
        isFirstLine: Boolean,
        linePos: Int,
        stack: StateStack,
        anchorPosition: Int
    ): MatchResult? {
        TODO()
    }

    fun matchInjections(
        injections: Array<Injection>,
        grammar: Grammar,
        lineText: OnigString,
        isFirstLine: Boolean,
        linePos: Int,
        stack: StateStack,
        anchorPosition: Int
    ): MatchInjectionsResult? {
        TODO()
    }

    fun prepareRuleSearch(
        rule: Rule,
        grammar: Grammar,
        endRegexSource: String?,
        allowA: Boolean,
        allowG: Boolean
    ): PrepareRuleResult {
        TODO()
    }

    fun prepareRuleWhileSearch(
        rule: BeginWhileRule,
        grammar: Grammar,
        endRegexSource: String?,
        allowA: Boolean,
        allowG: Boolean
    ): PrepareRuleResult {
        TODO()
    }

    fun getFindOptions(allowA: Boolean, allowG: Boolean): Int {
        TODO()
    }

    fun handleCaptures(
        grammar: Grammar,
        lineText: OnigString,
        isFirstLine: Boolean,
        stack: StateStack,
        lineTokens: LineTokens,
        captures: Array<CaptureRule?>,
        captureIndices: Array<OnigCaptureIndex>
    ) {
        TODO()
    }
}

