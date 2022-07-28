package org.codroid.textmate.rule

import org.codroid.textmate.utils.FindOption
import org.codroid.textmate.utils.OnigLib
import org.codroid.textmate.utils.OnigString

class CompiledRule(
    onigLib: OnigLib,
    private val regExps: Array<String>,
    private val rules: Array<RuleId>
) {

    private val scanner = onigLib.createOnigScanner(regExps)

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
        string: OnigString,
        startPosition: Int,
        options: FindOption
    ): FindNextMatchResult? {
        val result = this.scanner.findNextMatchSync(string, startPosition, options) ?: return null
        return FindNextMatchResult(ruleId = this.rules[result.index], captureIndices = result.captureIndices)
    }
}