package org.codroid.textmate.rule
private val HAS_BACK_REFERENCES = Regex("""\\(\d+)""")
private val BACK_REFERENCING_END = Regex("""\\(\d+)""")

class RuleId

const val endRuleId = -1
const val whileRuleId = -2

fun ruleIdFromNumber(id: Int): RuleId {
    TODO()
}

fun ruleIdToNumber(id: RuleId): Int {
    TODO()
}

abstract class Rule