package org.codroid.textmate.regex

class StandardRegex : RegexLib {
    override fun createScanner(source: Array<String>): RegexScanner {
        return StandardRegexScanner(source)
    }

    override fun createString(str: String): RegexString {
        return StandardRegexString(str)
    }
}

@JvmInline
value class StandardRegexString(override val content: String) : RegexString

class StandardRegexScanner(
    patterns: Array<String>
) : RegexScanner {

    private val searcher = StandardSearcher(patterns)

    override fun findNextMatchSync(string: RegexString, startPosition: Int): RegexMatch? {
        return StandardRegexMatch(searcher.search(string, startPosition) ?: return null, string)
    }

    override fun dispose() {

    }
}

class StandardRegexMatch(
    result: StandardResult,
    source: RegexString
) : RegexMatch {
    override val index: Int = result.indexInScanner
    override val ranges: Array<IntRange> = captureRanges(result)

    private fun captureRanges(result: StandardResult): Array<IntRange> {
        return result.matchResult.groups.map {
            it!!.range
        }.toTypedArray()
    }
}

data class StandardResult(val matchResult: MatchResult, var indexInScanner: Int) {
    val count: Int
        get() = matchResult.groups.size
}

class StandardSearcher(patterns: Array<String>) {

    private val regexes = patterns.map { Regex(it) }

    private var lastSearchString: RegexString? = null
    private var lastSearchPosition = -1
    private var lastSearchResult: StandardResult? = null

    fun search(source: RegexString, offset: Int): StandardResult? {
        var bestLocation = 0
        var bestResult: StandardResult? = null
        for ((idx, regex) in regexes.withIndex()) {
            val result = search_(regex, source, offset)
            if (result != null) {
                val location = result.matchResult.range.first
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

    private fun search_(regex: Regex, str: RegexString, position: Int): StandardResult? {
        val theLastSearchResult = lastSearchResult
        if (lastSearchString == str && lastSearchPosition <= position &&
            (theLastSearchResult == null || theLastSearchResult.matchResult.range.first >= position)
        ) {
            return theLastSearchResult
        }
        lastSearchString = str
        lastSearchPosition = position
        val found = regex.find(str.content)
        lastSearchResult = if (found != null) {
            StandardResult(found, -1)
        } else {
            null
        }
        return lastSearchResult
    }
}
