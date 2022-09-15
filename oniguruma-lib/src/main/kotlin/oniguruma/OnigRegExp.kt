package oniguruma

import org.codroid.textmate.regex.MatchGroup
import org.codroid.textmate.regex.MatchResult
import org.codroid.textmate.regex.RegularExp
import org.jcodings.specific.UTF8Encoding
import org.joni.Matcher
import org.joni.Option
import org.joni.Syntax
import org.joni.WarnCallback
import java.nio.charset.StandardCharsets
import kotlin.math.max

typealias RegexOnig = org.joni.Regex

class OnigRegExp(source: String) : RegularExp(source) {

    private var lastSearchString: OnigString? = null
    private var lastSearchPosition = -1
    private var lastSearchResult: OnigResult? = null

    private val regex: RegexOnig?

    init {
        val pattern = source.removePrefix("\\G").toByteArray(StandardCharsets.UTF_8)
        regex = try {
            RegexOnig(
                pattern,
                0,
                pattern.size,
                Option.CAPTURE_GROUP,
                UTF8Encoding.INSTANCE,
                Syntax.DEFAULT,
                WarnCallback.DEFAULT
            )
        } catch (_: Exception) {
            RegexOnig(
                ByteArray(0), 0, 0, Option.CAPTURE_GROUP, UTF8Encoding.INSTANCE, Syntax.DEFAULT, WarnCallback.DEFAULT
            )
        }
    }

    /**
     * Use it when you need save the last result.
     */
    fun search2(str: OnigString, position: Int): OnigResult? {
        val theLastSearchResult = lastSearchResult
        if (lastSearchString == str && lastSearchPosition <= position && (theLastSearchResult == null || theLastSearchResult.locationAt(
                0
            ) >= position)
        ) {
            return theLastSearchResult
        }
        lastSearchString = str
        lastSearchPosition = position
        lastSearchResult = searchForRegion(str.bytesUTF8, position, str.bytesCount)
        return lastSearchResult
    }

    private fun searchForRegion(data: ByteArray, position: Int, end: Int): OnigResult? {
        val matcher = regex?.matcher(data) ?: return null
        val status = matcher.search(position, end, 0)
        if (status != Matcher.FAILED) {
            val region = matcher.eagerRegion
            return OnigResult(region, -1)
        }
        return null
    }

    override fun search(input: String, startPosition: Int): MatchResult? {
        OnigString.create(input).let { onigStr ->
            searchForRegion(onigStr.bytesUTF8, startPosition, onigStr.bytesCount)?.region?.let { region ->
                val groups = mutableListOf<MatchGroup>()
                repeat(region.numRegs) {
                    ByteArray(region.end[it] - region.beg[it]) { i ->
                        onigStr.bytesUTF8[region.beg[it] + i]
                    }.let { value ->
                        groups.add(
                            MatchGroup(
                                String(value, Charsets.UTF_8), IntRange(
                                    onigStr.getCharIndexOfByte(max(0, region.beg[it])),
                                    onigStr.getCharIndexOfByte(max(0, region.end[it])) - 1
                                )
                            )
                        )
                    }
                }
                val resultRange =
                    IntRange(
                        onigStr.getCharIndexOfByte(max(0, region.beg[0])),
                        onigStr.getCharIndexOfByte(max(0, region.end[0])) - 1
                    )
                return MatchResult(
                    onigStr.content.substring(resultRange),
                    resultRange,
                    groups.toTypedArray()
                )
            }
        }
        return null
    }

    override fun containsMatchIn(input: String): Boolean {
        OnigString.create(input).let {
            return regex?.matcher(it.bytesUTF8)?.search(0, it.bytesCount, 0) != Matcher.FAILED
        }
    }


    override fun replace(origin: String, transform: (result: MatchResult) -> String): String {
        var search = search(origin)
        var replaced: String? = null
        var result = origin
        while (search != null) {
            if (replaced == null) {
                replaced = transform(search)
            }
            result = result.replaceRange(search.range, replaced)
            search = search(result, search.range.last + 1)
        }
        return result
    }
}