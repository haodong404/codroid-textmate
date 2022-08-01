package org.codroid.textmate.grammar

import org.codroid.textmate.Matcher
import org.codroid.textmate.Matchers
import org.codroid.textmate.Priority
import org.codroid.textmate.rule.RuleFactory
import org.codroid.textmate.rule.RuleFactoryHelper
import org.codroid.textmate.rule.RuleId

data class Injection(
    val debugSelector: String,
    val matcher: Matcher<Array<String>>,
    val priority: Priority,
    val ruleId: RuleId,
    val grammar: RawGrammar
)

fun collectInjections(
    result: MutableList<Injection>,
    selector: String,
    rule: RawRule,
    ruleFactoryHelper: RuleFactoryHelper,
    grammar: RawGrammar
) {
    val matchers = Matchers.create(selector, ::nameMatcher)
    val ruleId = RuleFactory.getCompiledRuleId(rule, ruleFactoryHelper, grammar.repository)
    for (matcher in matchers) {
        result.add(
            Injection(
                debugSelector = selector,
                matcher = matcher.matcher,
                ruleId = ruleId,
                grammar = grammar,
                priority = matcher.priority
            )
        )
    }
}