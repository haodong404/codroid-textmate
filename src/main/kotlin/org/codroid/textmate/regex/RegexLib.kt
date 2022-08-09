package org.codroid.textmate.regex

interface RegexLib {
    fun createScanner(source: Array<String>): RegexScanner

    fun compile(pattern: String): RegexExp
}

interface RegexScanner {
    fun findNextMatchSync(string: String, startPosition: Int): RegexMatch?
    fun dispose()
}

interface RegexMatch {
    val index: Int
    val ranges: Array<IntRange>
}

data class MatchResult(
    val value: String,
    val range: IntRange,
    val groups: Array<MatchGroup>,

    // Used in scanner.
    var indexInScanner: Int = -1
) {
    val count: Int
        get() = groups.size
}

data class MatchGroup(val value: String, val range: IntRange)

/**
 * Due to the difference in regular expression between Android and Java.
 * Something unexpected happened.
 * So I used this wrapper class for regex in this project.
 */
abstract class RegexExp(pattern: String) {

    /**
     * Indicates whether the regular expression can find at least one match in the specified input.
     */
    abstract fun containsMatchIn(input: String): Boolean

    /**
     * Returns a sequence of all occurrences of a regular expression within the input string,
     * beginning at the specified start index.
     */
    abstract fun search(input: String, startPosition: Int): MatchResult?

    /**
     * Search from start.
     */
    fun search(input: String): MatchResult? = search(input, 0)

    /**
     * Replaces all occurrences of this regular expression in the specified origin string with the result of the given function transform that takes MatchResult and returns a string to be used as a replacement for that match.
     */
    abstract fun replace(origin: String, transform: (result: MatchResult) -> String): String
}

typealias FindOption = Byte

object FindOptionConsts {
    const val None: FindOption = 0

    /**
     * equivalent of ONIG_OPTION_NOT_BEGIN_STRING: (str) isn't considered as begin of string (* fail \A)
     */
    const val NotBeginString: FindOption = 1

    /**
     * equivalent of ONIG_OPTION_NOT_END_STRING: (end) isn't considered as end of string (* fail \z, \Z)
     */
    const val NotEndString: FindOption = 2

    /**
     * equivalent of ONIG_OPTION_NOT_BEGIN_POSITION: (start) isn't considered as start position of search (* fail \G)
     */
    const val NotBeginPosition: FindOption = 4

    /**
     * used for debugging purposes.
     */
    const val DebugCall: FindOption = 8
}
