package org.codroid.textmate.oniguruma

import org.codroid.textmate.regex.*

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


class OnigLib : RegexLib {
    override fun createScanner(source: Array<String>): RegexScanner {
        return OnigScanner(source)
    }

    override fun createString(str: String): RegexString {
        return OnigString.create(str)
    }

}

fun getDefaultRegexLib(): RegexLib {
    return OnigLib()
}