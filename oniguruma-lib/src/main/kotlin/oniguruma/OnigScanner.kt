package oniguruma

import org.codroid.textmate.regex.RegexMatch
import org.codroid.textmate.regex.RegexScanner

class OnigScanner(patterns: Array<String>) : RegexScanner {

    private val searcher = OnigSearcher(patterns)

    override fun findNextMatchSync(string: String, startPosition: Int): RegexMatch? {
        OnigString.create(string).let {
            return OnigMatch(searcher.search(it, startPosition) ?: return null, it)
        }
    }

    override fun dispose() {

    }

}