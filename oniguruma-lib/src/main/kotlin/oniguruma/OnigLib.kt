package oniguruma

import org.codroid.textmate.regex.RegularExp
import org.codroid.textmate.regex.RegexLib
import org.codroid.textmate.regex.RegexMatch
import org.codroid.textmate.regex.RegexScanner

class OnigMatch(result: OnigResult, source: OnigString) : RegexMatch {
    override val index: Int = result.indexInScanner
    override val ranges: Array<IntRange> = captureIndicesOfMatch(result, source);

    private fun captureIndicesOfMatch(result: OnigResult, source: OnigString): Array<IntRange> {
        val resultCount = result.count()
        val captures = mutableListOf<IntRange>()
        for (i in 0 until resultCount) {
            val loc = result.locationAt(i)
            val captureStart = source.getCharIndexOfByte(loc)
            val captureEnd = source.getCharIndexOfByte(loc + result.lengthAt(i))
            captures.add(IntRange(captureStart, captureEnd - 1))
        }
        return captures.toTypedArray()
    }

}

class OnigLib : RegexLib {
    override fun createScanner(source: Array<String>): RegexScanner {
        return OnigScanner(source)
    }
    override fun compile(pattern: String): RegularExp {
        return OnigRegExp(pattern)
    }

}