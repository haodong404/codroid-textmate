package org.codroid.textmate

import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.NSNumber
import com.dd.plist.PropertyListParser
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream
import org.codroid.textmate.grammar.RawGrammar
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

private var CAPTURING_REGEX_SOURCE = Regex("""\$(\d+)|\$\{(\d+):/(downcase|upcase)}""")

object RegexSource {
    fun hasCaptures(regexSource: String?): Boolean {
        if (regexSource == null) return false
        return CAPTURING_REGEX_SOURCE.containsMatchIn(regexSource)
    }

    fun replaceCaptures(
        regexSource: String, captureSource: String, captureIndices: Array<IntRange>
    ): String {
        return regexSource.replace(CAPTURING_REGEX_SOURCE) {
            val capture = captureIndices.getOrNull(
                Integer.parseInt(
                    it.groups[1]?.value ?: it.groups[2]?.value ?: "0", 10
                )
            )
            return@replace if (capture != null) {
                var result = captureSource.substring(capture)
                // Remove leading dots that would make the selector invalid
                while (result[0] == '.') {
                    result = result.substring(1)
                }
                when (it.groups[3]?.value) {
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

object IntBooleanSerializer : KSerializer<Boolean> {

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("IntBoolean", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): Boolean {
        if (decoder is NSObjDecoder) {
            return deserializeWithNSObjectDecoder(decoder)
        }
        var intDecoded: Int? = null
        var boolDecoded: Boolean? = null
        try {
            intDecoded = decoder.decodeInt()
        } catch (_: Exception) {
        }

        try {
            boolDecoded = decoder.decodeBoolean()
        } catch (_: Exception) {
        }
        return if (intDecoded != null) {
            when (intDecoded) {
                1 -> true
                else -> false
            }
        } else {
            boolDecoded ?: false
        }
    }

    private fun deserializeWithNSObjectDecoder(decoder: NSObjDecoder): Boolean {
        val nsObj = decoder.currentElement()
        if (nsObj is NSNumber) {
            return if (nsObj.isBoolean) {
                nsObj.boolValue()
            } else {
                nsObj.intValue() == 1
            }
        }
        return false
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
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

inline fun <reified T : Any> parsePLIST(input: InputStream): T {
    val obj = PropertyListParser.parse(input)
    return decodeFromNSObject(obj)
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

fun <T> List<T>.every(test: (value: T) -> Boolean): Boolean {
    for (t in this) {
        if (!test(t)) {
            return false
        }
    }
    return true
}

fun <T> List<T>.some(test: (value: T) -> Boolean): Boolean {
    for (t in this) {
        if (test(t)) {
            return true
        }
    }
    return false
}

fun IntRange.endExclusive(): Int = this.last + 1

fun IntRange.distance(): Int = this.endExclusive() - this.first
