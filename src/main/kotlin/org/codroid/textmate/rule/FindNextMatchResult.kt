package org.codroid.textmate.rule

import org.codroid.textmate.utils.OnigCaptureIndex

data class FindNextMatchResult(val ruleId: RuleId, val captureIndices: Array<OnigCaptureIndex>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FindNextMatchResult

        if (ruleId != other.ruleId) return false
        if (!captureIndices.contentEquals(other.captureIndices)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ruleId.hashCode()
        result = 31 * result + captureIndices.contentHashCode()
        return result
    }
}
