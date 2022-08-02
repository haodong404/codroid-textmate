package org.codroid.textmate.rule

import org.codroid.textmate.RegexSource
import org.codroid.textmate.exceptions.TextMateException

class CaptureRule(
    override val id: RuleId,
    override val name: String?,
    override val contentName: String?,
    val retokenizeCapturedWithRuleId: RuleId
) : Rule() {
    override val nameIsCapturing: Boolean = RegexSource.hasCaptures(name)
    override val contentNameIsCapturing: Boolean = RegexSource.hasCaptures(contentName)

    override fun dispose() {
        // Nothing to dispose.
    }

    override fun collectPatterns(grammar: RuleRegistry, out: RegExpSourceList) {
        throw TextMateException("Not supported!")
    }

    override fun compile(grammar: RuleRegistryRegexLib, endRegexSource: String): CompiledRule {
        throw TextMateException("Not supported!")
    }

    override fun compileAG(
        grammar: RuleRegistryRegexLib,
        endRegexSource: String,
        allowA: Boolean,
        allowG: Boolean
    ): CompiledRule {
        throw TextMateException("Not supported!")
    }

}