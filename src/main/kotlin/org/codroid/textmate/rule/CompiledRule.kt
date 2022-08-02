package org.codroid.textmate.rule

import org.codroid.textmate.oniguruma.FindOption
import org.codroid.textmate.regex.RegexLib
import org.codroid.textmate.regex.RegexString

class CompiledRule(
    onigLib: RegexLib,
    private val regExps: Array<String>,
    private val rules: Array<RuleId>
) {

    private val scanner = onigLib.createScanner(regExps)

    fun dispose() {
        this.scanner.dispose()
    }

    override fun toString(): String {
        val result = StringBuilder()
        for ((idx, item) in this.rules.withIndex()) {
            result.append("   - " + item.id + ": " + regExps[idx])
            result.append("\n")
        }
        result.deleteAt(result.lastIndex)
        return result.toString()
    }

    fun findNextMatchSync(
        string: RegexString,
        startPosition: Int,
        option: FindOption
    ): FindNextMatchResult? {
        val result = this.scanner.findNextMatchSync(string, startPosition) ?: return null
        return FindNextMatchResult(ruleId = this.rules[result.index], captureIndices = result.ranges)
    }
}