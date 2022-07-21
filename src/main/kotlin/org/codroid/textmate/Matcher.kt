package org.codroid.textmate

data class MatcherWithPriority<T>(val matcher: Matcher<T>, val priority: Priority)

typealias Matcher<T> = (matcherInput: T) -> Boolean
typealias Priority = Byte


fun <T> createMatchers(
    selector: String,
    matchesName: (names: Array<String>, matcherInput: T) -> Boolean
): Array<MatcherWithPriority<T>> {
    TODO()
}

fun isIdentifier(token: String?): Boolean {
    if (token == null) {
        return false
    }
    return token.matches(Regex("[\\w.:]"))
}

fun newTokenizer(input: String): () -> String? {
    TODO()
}