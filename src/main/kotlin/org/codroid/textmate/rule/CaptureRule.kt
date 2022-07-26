package org.codroid.textmate.rule

import org.codroid.textmate.exceptions.TextMateException
import org.codroid.textmate.grammar.Location

class CaptureRule(
    override val location: Location?,
    override val id: RuleId,
    override val name: String?,
    override val contentName: String?,
    val retokenizeCapturedWithRuleId: RuleId
) : Rule(location, id, name, contentName) {

    override fun dispose() {
        // Nothing to dispose.
    }

    override fun collectPatterns(grammar: RuleRegistry, out: RegExpSourceList) {
        throw TextMateException("Not supported!")
    }

    override fun compile(grammar: RuleRegistryOnigLib, endRegexSource: String?): CompiledRule {
        throw TextMateException("Not supported!")
    }

    override fun compileAG(
        grammar: RuleRegistryOnigLib,
        endRegexSource: String?,
        allowA: Boolean,
        allowG: Boolean
    ): CompiledRule {
        throw TextMateException("Not supported!")
    }

}