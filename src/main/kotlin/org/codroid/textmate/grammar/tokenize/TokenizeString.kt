package org.codroid.textmate.grammar.tokenize

import org.codroid.textmate.DebugFlag
import org.codroid.textmate.grammar.Grammar
import org.codroid.textmate.grammar.LineTokens
import org.codroid.textmate.grammar.StateStack
import org.codroid.textmate.oniguruma.OnigString
import org.codroid.textmate.rule.BeginEndRule
import org.codroid.textmate.rule.BeginWhileRule
import org.codroid.textmate.rule.MatchRule
import org.codroid.textmate.rule.RuleId

/**
 * Tokenize a string
 * @param grammar
 * @param lineText
 * @param isFirstLine
 * @param linePos
 * @param stack
 * @param lineTokens
 * @param checkWhileConditions
 * @param timeLimit Use `0` to indicate no time limit
 * @returns the StackElement or StackElement.TIME_LIMIT_REACHED if the time limit has been reached
 */
fun tokenizeString(
    grammar: Grammar,
    lineText: OnigString,
    isFirstLine_: Boolean,
    linePos_: Int,
    stack_: StateStack,
    lineTokens: LineTokens,
    checkWhileConditions: Boolean,
    timeLimit: Int
): TokenizeStringResult {
    val lineLength = lineText.content.length
    var stop = false
    var anchorPosition = -1

    var stackClone = stack_
    var linePosClone = linePos_
    var isFirstLineClone = isFirstLine_
    if (checkWhileConditions) {
        val whileCheckResult = checkWhileConditions(grammar, lineText, isFirstLine_, linePos_, stack_, lineTokens)
        stackClone = whileCheckResult.stack;
        linePosClone = whileCheckResult.linePos;
        isFirstLineClone = whileCheckResult.isFirstLine;
        anchorPosition = whileCheckResult.anchorPosition;
    }

    fun scanNext() {
        if (DebugFlag) {
            println("")
            println(
                "@@scanNext $linePosClone: |${
                    lineText.content.substring(linePosClone).replace(Regex("\n$"), "\\n")
                }"
            )
        }
        val result = RuleMatching.matchRuleOrInjections(
            grammar,
            lineText,
            isFirstLineClone,
            linePosClone,
            stackClone,
            anchorPosition
        )
        if (result == null) {
            if (DebugFlag) {
                println("  no more matches.")
            }
            // No match
            lineTokens.produce(stackClone, lineLength)
            stop = true
            return
        }

        val captureIndices = result.captureIndices
        val matchedRuleId = result.matchedRuleId
        val hasAdvanced = if (captureIndices.isNotEmpty()) captureIndices[0].end > linePosClone else false

        if (matchedRuleId == RuleId.End) {
            // We matched the `end` for this rule => pop it
            val poppedRule = stackClone.getRule(grammar) as BeginEndRule
            if (DebugFlag) {
                println("  popping ${poppedRule.debugName()} - ${poppedRule.debugEndRegExp()}")
            }
            lineTokens.produce(stackClone, captureIndices[0].start)
            stackClone = stackClone.withContentNameScopesList(stackClone.nameScopesList)
            RuleMatching.handleCaptures(
                grammar, lineText, isFirstLineClone, stackClone, lineTokens,
                poppedRule.endCaptures, captureIndices
            )
            lineTokens.produce(stackClone, captureIndices[0].end)

            // pop
            val popped = stackClone
            stackClone = stackClone.parent!!
            anchorPosition = popped.anchorPos

            if (!hasAdvanced && popped.enterPos == linePosClone) {
                // Grammar pushed & popped a rule without advancing
                if (DebugFlag) {
                    println("[1] - Grammar is in an endless loop - Grammar pushed & popped a rule without advancing")
                }
                // See https://github.com/Microsoft/vscode-textmate/issues/12
                // Let's assume this was a mistake by the grammar author and the intent was to continue in this state
                stackClone = popped;
                lineTokens.produce(stackClone, lineLength)
                stop = true
                return
            }
        } else {
            // We matched a rule!
            val rule = grammar.getRule(matchedRuleId)
            lineTokens.produce(stackClone, captureIndices[0].start)
            val beforePush = stackClone
            // push it on the stack rule
            val scopeName = rule?.getName(lineText.content, captureIndices)
            val nameScopesList = stackClone.contentNameScopesList.pushAttributed(scopeName, grammar)
            stackClone = stackClone.push(
                matchedRuleId, linePosClone, anchorPosition, captureIndices[0].end == lineLength,
                null, nameScopesList, nameScopesList
            )
            when (rule) {
                is BeginEndRule -> {
                    if (DebugFlag) {
                        println("  pushing ${rule.debugName()} - ${rule.debugBeginRegExp()}")
                    }
                    RuleMatching.handleCaptures(
                        grammar,
                        lineText,
                        isFirstLineClone,
                        stackClone,
                        lineTokens,
                        rule.beginCaptures,
                        captureIndices
                    )
                    lineTokens.produce(stackClone, captureIndices[0].end)
                    anchorPosition = captureIndices[0].end
                    val contentName = rule.getContentName(lineText.content, captureIndices)
                    val contentNameScopesList = nameScopesList.pushAttributed(contentName, grammar)
                    stackClone = stackClone.withContentNameScopesList(contentNameScopesList)
                    if (rule.endHasBackReferences) {
                        val temp = rule.getEndWithResolvedBackReferences(lineText.content, captureIndices)
                        stackClone = stackClone.withEndRule(
                            temp
                        )
                    }
                    if (!hasAdvanced && beforePush.hasSameRuleAs(stackClone)) {
                        // Grammar pushed the same rule without advancing
                        if (DebugFlag) {
                            println(
                                "[2] - Grammar is in an endless loop - Grammar pushed the same rule without advancing"
                            )
                        }
                        stackClone = stackClone.pop()!!
                        lineTokens.produce(stackClone, lineLength)
                        stop = true
                        return
                    }
                }
                is BeginWhileRule -> {
                    if (DebugFlag) {
                        println("  pushing ${rule.debugName()}")
                    }
                    RuleMatching.handleCaptures(
                        grammar, lineText, isFirstLineClone, stackClone, lineTokens,
                        rule.beginCaptures, captureIndices
                    )
                    lineTokens.produce(stackClone, captureIndices[0].end)
                    anchorPosition = captureIndices[0].end
                    val contentName = rule.getContentName(lineText.content, captureIndices)
                    val contentNameScopesList = nameScopesList.pushAttributed(contentName, grammar)
                    stackClone = stackClone.withContentNameScopesList(contentNameScopesList)

                    if (rule.whileHasBackReferences) {
                        stackClone = stackClone.withEndRule(
                            rule.getWhileWithResolvedBackReferences(
                                lineText.content, captureIndices
                            )
                        )
                    }

                    if (!hasAdvanced && beforePush.hasSameRuleAs(stackClone)) {
                        // Grammar pushed the same rule without advancing
                        if (DebugFlag) {
                            println(
                                "[3] - Grammar is in an endless loop - Grammar pushed the same rule without advancing"
                            )
                        }
                        stackClone = stackClone.pop()!!
                        lineTokens.produce(stackClone, lineLength)
                        stop = true
                        return
                    }
                }
                else -> {
                    val matchingRule = rule as MatchRule
                    if (DebugFlag) {
                        println("  matched ${matchingRule.debugName()} - ${matchingRule.debugMatchRegExp()}")
                    }

                    RuleMatching.handleCaptures(
                        grammar,
                        lineText,
                        isFirstLineClone,
                        stackClone,
                        lineTokens,
                        matchingRule.captures,
                        captureIndices
                    )
                    lineTokens.produce(stackClone, captureIndices[0].end)

                    // pop rule immediately since it is a MatchRule
                    stackClone = stackClone.pop()!!
                    if (!hasAdvanced) {
                        // Grammar is not advancing, nor is it pushing/popping
                        if (DebugFlag) {
                            println(
                                "[4] - Grammar is in an endless loop - Grammar is not advancing, nor is it pushing/popping"
                            )
                        }
                        stackClone = stackClone.satePop()
                        lineTokens.produce(stackClone, lineLength)
                        stop = true
                        return
                    }
                }
            }
        }

        if (captureIndices[0].end > linePosClone) {
            // Advance stream
            linePosClone = captureIndices[0].end
            isFirstLineClone = false
        }
    }

    val startTime = System.currentTimeMillis()
    while (!stop) {
        if (timeLimit != 0) {
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime > timeLimit) {
                return TokenizeStringResult(stackClone, true)
            }
        }
        scanNext() // potentially modifies linePos && anchorPosition
    }
    return TokenizeStringResult(stackClone, false)
}

data class TokenizeStringResult(
    val stack: StateStack, val stoppedEarly: Boolean
)
