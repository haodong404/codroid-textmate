package org.codroid.textmate.utils

fun <T> clone(something: T): T {
    TODO()
}

private fun Any.doClone(): Any {
    TODO()
}

private fun Array<Any>.cloneArray(): Any {
    TODO()
}

private fun Any.cloneObj(): Any {
    TODO()
}

fun Any.mergeObjects(vararg sources: Array<Any>): Any {
    TODO()
}

fun basename(path: String): String {
    TODO()
}

private var CAPTURING_REGEX_SOURCE = "\\\$(\\d+)|\\\${(\\d+):\\/(downcase|upcase)}";

class RegexSource {}

/**
 * A union of given const enum values.
 */
typealias OrMask = Int

fun strcmp(a: String, b: String): Int {
    TODO()
}

fun strArrCmp(a: Array<String>?, b: Array<String>?): Int {
    TODO()
}

fun isValidHexColor(hex: String): Boolean {
    TODO()
}

/**
 * Escapes regular expression characters in a given string
 */
fun escapeRegExpCharacters(value: String): String {
    TODO()
}

class CachedFn<K, V> {}

private var performance: (() -> Int)? = null

val performanceNow =
    if (performance == null) {        // performance.now() is not available in this environment, so use Date.now()
        { System.currentTimeMillis() }
    } else {
        performance!!
    }
