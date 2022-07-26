@file:OptIn(ExperimentalUnsignedTypes::class)

package org.codroid.textmate

data class Token(
    val startIndex: Int, val endIndex: Int, val scopes: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Token

        if (startIndex != other.startIndex) return false
        if (endIndex != other.endIndex) return false
        if (!scopes.contentEquals(other.scopes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startIndex
        result = 31 * result + endIndex
        result = 31 * result + scopes.contentHashCode()
        return result
    }
}

data class TokenizeLineResult(
    val tokens: Array<Token>,
    /**
     * The `prevState` to be passed on to the next line tokenization.
     */
    val ruleStack: StateStack,
    /**
     * Did tokenization stop early due to reaching the time limit.
     */
    val stoppedEarly: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TokenizeLineResult

        if (!tokens.contentEquals(other.tokens)) return false
        if (ruleStack != other.ruleStack) return false
        if (stoppedEarly != other.stoppedEarly) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tokens.contentHashCode()
        result = 31 * result + ruleStack.hashCode()
        result = 31 * result + stoppedEarly.hashCode()
        return result
    }
}

data class TokenizeLineResult2(
    /**
     * The tokens in binary format. Each token occupies two array indices. For token i:
     *  - at offset 2*i => startIndex
     *  - at offset 2*i + 1 => metadata
     *
     */
    val tokens: UIntArray,
    /**
     * The `prevState` to be passed on to the next line tokenization.
     */
    val ruleStack: StateStack,
    /**
     * Did tokenization stop early due to reaching the time limit.
     */
    val stoppedEarly: Boolean
)

typealias TokenTypeMap = HashMap<String, StandardTokenType>