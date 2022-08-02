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
