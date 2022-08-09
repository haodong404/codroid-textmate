package org.codroid.textmate.grammar.tokenize

import org.codroid.textmate.DebugFlag
import org.codroid.textmate.endExclusive
import org.codroid.textmate.grammar.Grammar
import org.codroid.textmate.grammar.LineTokens
import org.codroid.textmate.grammar.StateStack
import org.codroid.textmate.rule.BeginWhileRule
import org.codroid.textmate.rule.RuleId

/**
 * Walk the stack from bottom to top, and check each while condition in this order.
 * If any fails, cut off the entire stack above the failed while condition. While conditions
 * may also advance the linePosition.
 */
fun checkWhileConditions(
    grammar: Grammar,
    lineText: String,
    isFirstLine_: Boolean,
    linePos_: Int,
    stack_: StateStack,
    lineTokens: LineTokens
): WhileCheckResult {
    var stack = stack_
    var linePos = linePos_
    var isFirstLine = isFirstLine_
    var anchorPosition = if (stack.beginRuleCapturedEOL) 0 else -1
    val whileRules = mutableListOf<Pair<StateStack, BeginWhileRule>>()
    var node: StateStack? = stack
    while (node != null) {
        val nodeRule = node.getRule(grammar)
        if (nodeRule is BeginWhileRule) {
            whileRules.add(Pair(node, nodeRule))
        }
        node = node.pop()
    }
    var whileRule: Pair<StateStack, BeginWhileRule>? = whileRules.removeLastOrNull()
    while (whileRule != null) {
        val (ruleScanner, findOptions) = RuleMatching.prepareRuleWhileSearch(
            whileRule.second,
            grammar,
            whileRule.first.endRule,
            isFirstLine,
            linePos == anchorPosition
        )
        val result = ruleScanner.findNextMatchSync(lineText, linePos, findOptions)
        if (DebugFlag) {
            println("  scanning for while rule")
            println(ruleScanner.toString())
        }
        if (result != null) {
            val matchedRuleId = result.ruleId
            if (matchedRuleId != RuleId.While) {
                // we shouldn't end up here
                stack = whileRule.first.pop()!!
                break
            }
            if (result.captureIndices.isNotEmpty()) {
                lineTokens.produce(whileRule.first, result.captureIndices[0].first)
                RuleMatching.handleCaptures(
                    grammar, lineText, isFirstLine, whileRule.first, lineTokens,
                    whileRule.second.whileCaptures, result.captureIndices
                )
                lineTokens.produce(whileRule.first, result.captureIndices[0].endExclusive())
                anchorPosition = result.captureIndices[0].endExclusive()
                if (result.captureIndices[0].endExclusive() > linePos) {
                    linePos = result.captureIndices[0].endExclusive()
                    isFirstLine = false
                }
            }
        } else {
            if (DebugFlag) {
                println("  popping ${whileRule.second.debugName()} - ${whileRule.second.debugWhileRegExp()}")
            }
            stack = whileRule.first.pop()!!
            break
        }
        whileRule = whileRules.getOrNull(whileRules.lastIndex)
        whileRules.removeLastOrNull()
    }
    return WhileCheckResult(stack, linePos, anchorPosition, isFirstLine)
}

data class WhileCheckResult(val stack: StateStack, val linePos: Int, val anchorPosition: Int, val isFirstLine: Boolean)