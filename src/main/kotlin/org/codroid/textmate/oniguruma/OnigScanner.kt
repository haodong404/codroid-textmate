package org.codroid.textmate.oniguruma

class OnigScanner(patterns: Array<String>) {

    private val searcher = OnigSearcher(patterns)

    fun findNextMatchSync(string: OnigString, startPosition: Int, option: FindOption): OnigMatch? {
        return OnigMatch(searcher.search(string, startPosition, option) ?: return null, string)
    }

    fun dispose() {

    }
}