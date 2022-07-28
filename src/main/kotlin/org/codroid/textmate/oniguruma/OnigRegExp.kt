package org.codroid.textmate.oniguruma

import org.jcodings.specific.UTF8Encoding
import org.joni.Matcher
import org.joni.Option
import org.joni.Syntax
import org.joni.WarnCallback
import java.nio.charset.StandardCharsets

typealias RegexOnig = org.joni.Regex

class OnigRegExp(source: String) {
    private var lastSearchString: OnigString? = null
    private var lastSearchPosition = -1
    private var lastSearchResult: OnigResult? = null

    private val regex: RegexOnig

    init {
        val pattern = source.toByteArray(StandardCharsets.UTF_8)
        regex = RegexOnig(
            pattern,
            0,
            pattern.size,
            Option.CAPTURE_GROUP,
            UTF8Encoding.INSTANCE,
            Syntax.DEFAULT,
            WarnCallback.DEFAULT
        )
    }

    fun search(str: OnigString, position: Int): OnigResult? {
        val theLastSearchResult = lastSearchResult
        if (lastSearchString == str && lastSearchPosition <= position &&
            (theLastSearchResult == null || theLastSearchResult.locationAt(0) >= position)
        ) {
            return theLastSearchResult
        }
        lastSearchString = str
        lastSearchPosition = position
        lastSearchResult = search(str.bytesUTF8, position, str.bytesCount)
        return lastSearchResult
    }

    private fun search(data: ByteArray, position: Int, end: Int): OnigResult? {
        val matcher = regex.matcher(data)
        val status = matcher.search(position, end, Option.DEFAULT)
        if (status != Matcher.FAILED) {
            val region = matcher.eagerRegion
            return OnigResult(region, -1)
        }
        return null
    }
}