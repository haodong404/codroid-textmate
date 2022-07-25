package org.codroid.textmate.rule

data class CompilePatternsResult(val patterns: Array<RuleId>, val hasMissingPatterns: Boolean)