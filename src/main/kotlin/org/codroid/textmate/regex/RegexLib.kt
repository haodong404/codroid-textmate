package org.codroid.textmate.regex

interface RegexLib {
    fun createScanner(source: Array<String>): RegexScanner
    fun createString(str: String): RegexString
}

interface RegexString {
    val content: String
}

interface RegexScanner {
    fun findNextMatchSync(string: RegexString, startPosition: Int): RegexMatch?
    fun dispose()
}

interface RegexMatch {
    val index: Int
    val ranges: Array<IntRange>
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
