package org.codroid.textmate.regex

import org.codroid.textmate.DebugFlag
import java.util.regex.PatternSyntaxException

class StandardRegex : RegexLib {
    override fun createScanner(source: Array<String>): RegexScanner {
        return StandardRegexScanner(source)
    }

    override fun compile(pattern: String): RegexExp {
        return StandardRegexExp(pattern)
    }
}

class StandardRegexScanner(
    patterns: Array<String>
) : RegexScanner {

    private val searcher = StandardSearcher(patterns)

    override fun findNextMatchSync(string: String, startPosition: Int): RegexMatch? {
        return StandardRegexMatch(searcher.search(string, startPosition) ?: return null, string)
    }

    override fun dispose() {

    }
}

class StandardRegexMatch(
    result: MatchResult,
    source: String
) : RegexMatch {
    override val index: Int = result.indexInScanner
    override val ranges: Array<IntRange> = captureRanges(result)

    private fun captureRanges(result: MatchResult): Array<IntRange> {
        return result.groups.map {
            it.range
        }.toTypedArray()
    }
}

data class StandardResult(val matchResult: MatchResult, var indexInScanner: Int) {
    val count: Int
        get() = matchResult.groups.size
}

class StandardSearcher(patterns: Array<String>) {

    private val regexes = patterns.map { StandardRegexExp(it) }

    fun search(source: String, offset: Int): MatchResult? {
        var bestLocation = 0
        var bestResult: MatchResult? = null
        for ((idx, regex) in regexes.withIndex()) {
            val result = regex.search2(source, offset)
            if (result != null && result.count > 0) {
                val location = result.range.first
                if (bestResult == null || location < bestLocation) {
                    bestLocation = location
                    bestResult = result
                    bestResult.indexInScanner = idx
                }
                if (location == offset) {
                    break
                }
            }
        }
        return bestResult
    }
}

class StandardRegexExp(pattern: String) : RegexExp(pattern) {

    private var lastSearchString: String? = null
    private var lastSearchPosition = -1
    private var lastSearchResult: MatchResult? = null

    private val regex = try {
        Regex(pattern)
    } catch (e: PatternSyntaxException) {
        if (DebugFlag) {
            System.err.println("An illegal regular expression was found: ${e.pattern}")
        }
        null
    }

    override fun containsMatchIn(input: String): Boolean {
        return regex?.containsMatchIn(input) ?: false
    }

    fun search2(input: String, startPosition: Int): MatchResult? {
        val theLastSearchResult = lastSearchResult
        if (lastSearchString == input && lastSearchPosition <= startPosition &&
            (theLastSearchResult == null || theLastSearchResult.range.first >= startPosition)
        ) {
            return theLastSearchResult
        }
        lastSearchString = input
        lastSearchPosition = startPosition
        val found = regex?.find(input, startPosition)
        lastSearchResult = search(input, startPosition)
        return lastSearchResult
    }

    override fun search(input: String, startPosition: Int): MatchResult? {
        regex?.find(input, startPosition)?.let {
            val groups = it.groups.map { group ->
                MatchGroup(group?.value ?: "", group?.range ?: 0..0)
            }
            return MatchResult(it.value, it.range, groups.toTypedArray())
        }
        return null
    }

    override fun replace(origin: String, transform: (result: MatchResult) -> String): String {
        regex?.run {
            return regex.replace(origin) r@{
                val result = it.groups.map { group ->
                    MatchGroup(group?.value ?: "", group?.range ?: 0..0)
                }
                return@r transform(MatchResult(it.value, it.range, result.toTypedArray()))
            }
        }
        return origin
    }

}