package oniguruma

import org.codroid.textmate.regex.RegexMatch
import org.codroid.textmate.regex.RegexScanner
import org.codroid.textmate.regex.RegexString

class OnigScanner(patterns: Array<String>) : RegexScanner {

    private val searcher = OnigSearcher(patterns)

    override fun findNextMatchSync(string: RegexString, startPosition: Int): RegexMatch? {
        return OnigMatch(searcher.search(string as OnigString, startPosition) ?: return null, string)
    }

    override fun dispose() {

    }

}