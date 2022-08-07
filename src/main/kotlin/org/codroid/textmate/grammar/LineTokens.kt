package org.codroid.textmate.grammar

import org.codroid.textmate.*
import org.codroid.textmate.theme.FontStyleConsts

/**
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/caab3de34a8cc7182141c9e31e0f42b96a3a1bac/src/grammar/grammar.ts#L855">
 *     src/grammar/grammar.ts#L855</a>
 */
class LineTokens(
    val emitBinaryTokens: Boolean,
    lineText: String,
    val tokenTypeOverride: Array<TokenTypeMatcher>,
    val balancedBracketSelectors: BalancedBracketSelectors? = null
) {
    private val lineText: String? = if (DebugFlag) lineText else null
    private val tokens = mutableListOf<Token>()
    private val binaryTokens = mutableListOf<UInt>()
    private var lastTokenEndIndex = 0

    fun produce(stack: StateStack, endIndex: Int) = this.produceFromScopes(stack.contentNameScopesList, endIndex)

    fun produceFromScopes(scopeList: AttributedScopeStack, endIndex: Int) {
        if (this.lastTokenEndIndex >= endIndex) {
            return
        }
        if (this.emitBinaryTokens) {
            var metadata = scopeList.tokenAttributes
            var containsBalancedBrackets = false
            if (this.balancedBracketSelectors?.getMatchersAlways() == true) {
                containsBalancedBrackets = true
            }
            if (this.tokenTypeOverride.isNotEmpty() || (this.balancedBracketSelectors != null && !this.balancedBracketSelectors.getMatchersAlways() && !this.balancedBracketSelectors.getMatchesNever())) {
                // Only generate scope array when required to improve performance
                val scopes = scopeList.getScopeNames()
                for (tokenType in this.tokenTypeOverride) {
                    if (tokenType.matcher(scopes)) {
                        metadata = EncodedTokenAttributes.set(
                            metadata, 0u, toOptionalTokenType(tokenType.type), null, FontStyleConsts.NotSet, 0u, 0u
                        )
                    }
                }
                if (this.balancedBracketSelectors != null) {
                    containsBalancedBrackets = this.balancedBracketSelectors.match(scopes)
                }
            }
            if (containsBalancedBrackets) {
                metadata = EncodedTokenAttributes.set(
                    metadata, 0u, OptionalStandardTokenTypeConsts.NotSet, true, FontStyleConsts.NotSet, 0u, 0u
                )
            }
            if (this.binaryTokens.isNotEmpty() && this.binaryTokens.last() == metadata) {
                // no need to push a token with the same metadata
                this.lastTokenEndIndex = endIndex
                return
            }
            if (DebugFlag) {
                scopeList.getScopeName().let {
                    println(
                        "  token: | ${
                            this.lineText?.substring(this.lastTokenEndIndex, endIndex)?.replace(Regex("\n$"), "\\n")
                        }"
                    )
                    for (scope in it) {
                        println("      * $scope")
                    }
                }
            }
            this.binaryTokens.add(this.lastTokenEndIndex.toUInt())
            this.binaryTokens.add(metadata)
            this.lastTokenEndIndex = endIndex
            return
        }
        val scopes = scopeList.getScopeNames()
        if (DebugFlag) {
            println(
                "  token: |${this.lineText?.substring(this.lastTokenEndIndex, endIndex)?.replace(Regex("\n$"), "\\n")}|"
            )
            for (scope in scopes) {
                println("      * $scope")
            }
        }
        this.tokens.add(
            Token(
                startIndex = this.lastTokenEndIndex, endIndex = endIndex,
                // value: lineText.substring(lastTokenEndIndex, endIndex),
                scopes = scopes
            )
        )
        this.lastTokenEndIndex = endIndex
    }

    fun getResult(stack: StateStack, lineLength: Int): Array<Token> {
        if (this.tokens.isNotEmpty() && this.tokens.last().startIndex == lineLength - 1) {
            // pop produced token for newline
            this.tokens.removeLast()
        }

        if (this.tokens.isEmpty()) {
            this.lastTokenEndIndex = -1
            this.produce(stack, lineLength - 1)
            this.tokens.last().startIndex = 0
        } else {
            if (this.tokens.last().endIndex == lineLength) {
                this.tokens.last().endIndex = lineLength - 1
            }
        }
        return this.tokens.toTypedArray()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun getBinaryResult(stack: StateStack, lineLength: Int): UIntArray {
        if (this.binaryTokens.isNotEmpty() && this.binaryTokens[this.binaryTokens.size - 2] == (lineLength - 1).toUInt()) {
            // pop produced token for newline
            this.binaryTokens.removeLast()
        }
        if (this.binaryTokens.isEmpty()) {
            this.lastTokenEndIndex = -1
            this.produce(stack, lineLength)
            this.binaryTokens[this.binaryTokens.size - 2] = 0u
        }
        val result = UIntArray(this.binaryTokens.size)
        for ((idx, item) in this.binaryTokens.withIndex()) {
            result[idx] = item
        }
        return result
    }
}