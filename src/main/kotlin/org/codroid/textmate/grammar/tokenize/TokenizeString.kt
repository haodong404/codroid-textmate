package org.codroid.textmate.grammar.tokenize

import org.codroid.textmate.grammar.Grammar
import org.codroid.textmate.grammar.LineTokens
import org.codroid.textmate.grammar.StateStack
import org.codroid.textmate.utils.OnigString


fun tokenizeString(
    grammar: Grammar,
    lineText: OnigString,
    isFirstLine: Boolean,
    linePos: Int,
    stack: StateStack,
    lineTokens: LineTokens,
    checkWhileConditions: Boolean,
    timeLimit: Int
): TokenizeStringResult {
    TODO()
}

data class TokenizeStringResult(
    val stack: StateStack, val stoppedEarly: Boolean
)