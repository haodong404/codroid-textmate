package org.codroid.textmate.rule

data class CompilePatternsResult(val patterns: Array<RuleId>, val hasMissingPatterns: Boolean) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompilePatternsResult

        if (!patterns.contentEquals(other.patterns)) return false
        if (hasMissingPatterns != other.hasMissingPatterns) return false

        return true
    }

    override fun hashCode(): Int {
        var result = patterns.contentHashCode()
        result = 31 * result + hasMissingPatterns.hashCode()
        return result
    }
}