package org.codroid.textmate

import com.dd.plist.PropertyListParser
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.codroid.textmate.grammar.RawCaptures
import org.codroid.textmate.grammar.RawGrammar
import org.codroid.textmate.grammar.RawRepository
import org.codroid.textmate.oniguruma.OnigCaptureIndex
import java.io.InputStream

fun basename(path: String): String {
    return when (val idx = path.lastIndexOf('/').inv() or path.lastIndexOf('\\').inv()) {
        0 -> path
        (path.length - 1).inv() -> basename(path.substring(0, path.length - 1))
        else -> path.substring(idx.inv() + 1)
    }
}

inline fun <reified T : Cloneable> MutableList<T>.clone(): MutableList<T> {
    val cloned = MutableList(this.size) {
        this[it]
    }
    return cloned
}

private var CAPTURING_REGEX_SOURCE = Regex("\\\$(\\d+)|\\\$*(\\d+):/(downcase|upcase)}")

object RegexSource {
    fun hasCaptures(regexSource: String?): Boolean {
        if (regexSource == null) return false
        return CAPTURING_REGEX_SOURCE.containsMatchIn(regexSource)
    }

    fun replaceCaptures(
        regexSource: String, captureSource: String, captureIndices: Array<OnigCaptureIndex>
    ): String {
        return regexSource.replace(CAPTURING_REGEX_SOURCE) {
            val capture = captureIndices.getOrNull(
                Integer.parseInt(
                    it.groupValues.getOrNull(0) ?: it.groupValues.getOrElse(1) { "0" }, 10
                )
            )
            return@replace if (capture != null) {
                var result = captureSource.substring(capture.start, capture.end)
                // Remove leading dots that would make the selector invalid
                while (result[0] == '.') {
                    result = result.substring(1)
                }
                when (it.groupValues.getOrElse(2) { false }) {
                    "downcase" -> result.lowercase()
                    "upcase" -> result.uppercase()
                    else -> result
                }
            } else {
                it.value
            }
        }
    }
}

/**
 * A union of given const enum values.
 */
typealias OrMask = Int

fun strcmp(a: String, b: String): Int {
    return if (a < b) -1
    else if (a > b) 1
    else 0
}

fun strLisCmp(a: List<String>?, b: List<String>?): Int {
    return strArrCmp(a?.toTypedArray(), b?.toTypedArray())
}

fun strArrCmp(a: Array<String>?, b: Array<String>?): Int {
    if (a == null && b == null) return 0
    else if (a == null) return -1
    else if (b == null) return 1

    val len1 = a.size
    val len2 = b.size
    if (len1 == len2) {
        for (i in 0 until len1) {
            val res = strcmp(a[i], b[i])
            if (res != 0) {
                return res
            }
        }
        return 0
    }
    return len1 - len2
}

fun isValidHexColor(hex: String): Boolean {
    return if (Regex("^#[\\da-f]{6}\$", RegexOption.IGNORE_CASE).containsMatchIn(hex)) {
        true
    } else if (Regex("^#[\\da-f]{8}\$", RegexOption.IGNORE_CASE).containsMatchIn(hex)) {
        true
    } else if (Regex("^#[\\da-f]{3}\$", RegexOption.IGNORE_CASE).containsMatchIn(hex)) {
        true
    } else {
        Regex("^#[\\da-f]{4}\$", RegexOption.IGNORE_CASE).containsMatchIn(hex)
    }

}

/**
 * Escapes regular expression characters in a given string
 */
fun escapeRegExpCharacters(value: String): String =
    value.replace(Regex("[\\-\\\\{}*+?|^$.,\\[\\]()#\\s]")) {
        "\\${it.value}"
    }

class CachedFn<K, V>(private val fn: (key: K) -> V) {
    private val cache = HashMap<K, V>()

    fun get(key: K): V {
        if (cache.containsKey(key)) {
            return cache[key]!!
        }
        val value = fn(key)
        cache[key] = value
        return value
    }
}

private var performance: (() -> Int)? = null

val performanceNow =
    if (performance == null) {        // performance.now() is not available in this environment, so use Date.now()
        { System.currentTimeMillis() }
    } else {
        performance!!
    }

fun Byte.toBoolean(): Boolean {
    return when (this) {
        0.toByte() -> false
        else -> true
    }
}

object RawCapturesMapSerializer : JsonTransformingSerializer<RawCaptures>(RawCaptures.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return dynamicMap(element)
    }
}

object RawRepositorySerializer : JsonTransformingSerializer<RawRepository>(RawRepository.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return dynamicMap(element)
    }
}

private fun dynamicMap(element: JsonElement): JsonElement {
    if (element is JsonObject) {
        return JsonObject(
            mapOf(Pair("map", element))
        )
    }
    return element
}

inline fun <reified T> parsePLIST(input: InputStream): T {
    return PropertyListParser.parse(input).toJavaObject(T::class.java)
}

val json = Json {
    ignoreUnknownKeys = true
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> parseJson(input: InputStream): T {
    return json.decodeFromStream(input)
}

fun parseRawGrammar(input: InputStream, filePath: String): RawGrammar {
    if (Regex("\\.json$").containsMatchIn(filePath)) {
        return parseJson(input)
    }
    return parsePLIST(input)
}