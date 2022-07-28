package org.codroid.textmate.grammar.tokenize

import org.codroid.textmate.grammar.Grammar
import org.codroid.textmate.grammar.LineTokens
import org.codroid.textmate.grammar.StateStack
import org.codroid.textmate.utils.OnigString

/**
 * Walk the stack from bottom to top, and check each while condition in this order.
 * If any fails, cut off the entire stack above the failed while condition. While conditions
 * may also advance the linePosition.
 */
fun checkWhileConditions(
    grammar: Grammar,
    lineText: OnigString,
    isFirstLine: Boolean,
    linePos: Int,
    stack: StateStack,
    lineTokens: LineTokens
): WhileCheckResult {
    TODO()
}

data class WhileCheckResult(val stack: StateStack, val linePos: Int, val anchorPosition: Int, val isFirstLine: Boolean)