package org.codroid.textmate.oniguruma

import org.codroid.textmate.regex.RegexLib
import org.codroid.textmate.regex.StandardRegex

interface OnigLib {
    fun createOnigScanner(source: Array<String>): OnigScanner
    fun createOnigString(str: String): OnigString
}

data class OnigCaptureIndex(val index: Int, val start: Int, val end: Int) {
    val length = this.end - this.start
}

class OnigMatch(result: OnigResult, source: OnigString) {
    val index: Int = result.indexInScanner
    val captureIndices: Array<OnigCaptureIndex> = captureIndicesOfMatch(result, source);

    private fun captureIndicesOfMatch(result: OnigResult, source: OnigString): Array<OnigCaptureIndex> {
        val resultCount = result.count()
        val captures = mutableListOf<OnigCaptureIndex>()
        for (i in 0 until resultCount) {
            val loc = result.locationAt(i)
            val captureStart = source.getCharIndexOfByte(loc)
            val captureEnd = source.getCharIndexOfByte(loc + result.lengthAt(i))
            captures.add(OnigCaptureIndex(i, captureStart, captureEnd))
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


class DefaultOnigLib : OnigLib {
    override fun createOnigScanner(source: Array<String>): OnigScanner {
        return OnigScanner(source)
    }

    override fun createOnigString(str: String): OnigString {
        return OnigString.create(str)
    }

}

fun getDefaultRegexLib(): RegexLib {
    return StandardRegex()
}