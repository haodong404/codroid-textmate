package org.codroid.textmate.grammar

import org.codroid.textmate.Matcher
import org.codroid.textmate.Priority
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
    result: Array<Injection>,
    selector: String,
    rule: RawRule,
    ruleFactoryHelper: RuleFactoryHelper,
    grammar: RawGrammar
) {
    TODO()
}