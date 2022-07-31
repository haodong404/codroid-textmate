package org.codroid.textmate

data class MatcherWithPriority<T>(val matcher: Matcher<T>, val priority: Priority)

typealias Matcher<T> = (matcherInput: T) -> Boolean
typealias Priority = Byte

class Matchers<T>(selector: String, val matchesName: (names: List<String>, matcherInput: T) -> Boolean) {
    val matchers = mutableListOf<MatcherWithPriority<T>>()
    private val nextTokenizerFun: () -> String?
    private var token: String? = null

    companion object {
        fun isIdentifier(token: String?): Boolean {
            if (token == null) {
                return false
            }
            return token.contains(Regex("[\\w.:]+"))
        }

        fun newTokenizer(input: String): () -> String? {
            val regex = Regex("([LR]:|[\\w.:][\\w.:\\-]*|[,|\\-()])")
            val match = regex.findAll(input)
            val it = match.iterator()
            return result@{
                if (!it.hasNext()) return@result null
                val next = it.next()
                return@result next.value
            }
        }

        fun <T> create(
            selector: String,
            matchesName: (names: List<String>, matcherInput: T) -> Boolean
        ): List<MatcherWithPriority<T>> {
            return Matchers(selector, matchesName).matchers
        }
    }

    init {
        nextTokenizerFun = newTokenizer(selector)
        token = nextTokenizerFun()
        while (token != null) {
            var priority: Priority = 0
            if (token!!.length == 2 && token!![1] == ':') {
                when (token!![0]) {
                    'R' -> priority = 1
                    'L' -> priority = -1
                    else -> println("Unknown priority $token in scope selector")
                }
                token = nextTokenizerFun()
            }
            val matcher = parseConjunction()
            matchers.add(MatcherWithPriority(matcher, priority))
            if (token != ",") break
            token = nextTokenizerFun()
        }
    }

    private fun parseOperand(): Matcher<T>? {
        if (token == "-") {
            token = nextTokenizerFun()
            val expressionToNegate = parseOperand()
            return result@{
                return@result expressionToNegate != null && !expressionToNegate.invoke(it)
            }
        }
        if (token == "(") {
            token = nextTokenizerFun()
            val expressionInParents = parseInnerExpression();
            if (token == ")") {
                token = nextTokenizerFun()
            }
            return expressionInParents
        }
        if (isIdentifier(token)) {
            val identifiers = mutableListOf<String>()
            do {
                token?.let { identifiers.add(it) }
                token = nextTokenizerFun()
            } while (isIdentifier(token))
            return result@{
                return@result matchesName(identifiers, it)
            }
        }
        return null
    }

    private fun parseConjunction(): Matcher<T> {
        val matchers = arrayListOf<Matcher<T>>()
        var matcher = parseOperand()
        while (matcher != null) {
            matchers.add(matcher)
            matcher = parseOperand()
        }
        return result@{
            var temp = true
            for (item in matchers) {
                if (!item(it)) {
                    temp = false
                    break
                }
            }
            return@result temp
        } // and
    }

    private fun parseInnerExpression(): Matcher<T> {
        val matchers = arrayListOf<Matcher<T>>()
        var matcher = parseConjunction()
        while (true) {
            matchers.add(matcher)
            if (token == "|" || token == ",") {
                do {
                    token = nextTokenizerFun()
                } while (token == "|" || token == ",")
            } else {
                break
            }
            matcher = parseConjunction()
        }
        return result@{
            for (item in matchers) {
                if (item(it)) {
                    return@result true
                }
            }
            return@result false
        }
    } // or
}