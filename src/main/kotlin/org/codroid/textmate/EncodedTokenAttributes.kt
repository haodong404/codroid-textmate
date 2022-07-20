package org.codroid.textmate


typealias EncodedToken = UInt

object EncodedTokenAttributes {
    fun toBinaryStr(encodedTokenAttributes: EncodedToken): String {
        var r = encodedTokenAttributes.toString(2)
        while (r.length < 32) {
            r = "0$r"
        }
        return r
    }

    fun print(encodedTokenAttributes: EncodedToken) {
        val languageId = getLanguageId(encodedTokenAttributes);
        val tokenType = getTokenType(encodedTokenAttributes);
        val fontStyle = getFontStyle(encodedTokenAttributes);
        val foreground = getForeground(encodedTokenAttributes);
        val background = getBackground(encodedTokenAttributes);

        println(
            """{
            languageId: $languageId,
            tokenType: $tokenType,
            fontStyle: $fontStyle,
            foreground: $foreground,
            background: $background
            }
        """.trimIndent()
        )
    }

    fun getLanguageId(encodedTokenAttributes: EncodedToken): UInt {
        TODO()
    }

    fun getTokenType(encodedTokenAttributes: EncodedToken): StandardTokenType {
        TODO()
    }

    fun containsBalancedBrackets(encodedTokenAttributes: EncodedToken): Boolean {
        TODO()
    }

    fun getFontStyle(encodedTokenAttributes: EncodedToken): FontStyle {
        TODO()
    }

    fun getForeground(encodedTokenAttributes: EncodedToken): UInt {
        TODO()
    }

    fun getBackground(encodedTokenAttributes: EncodedToken): UInt {
        TODO()
    }

    fun set(
        encodedTokenAttributes: EncodedToken,
        languageId: UInt,
        tokenType: OptionalStandardTokenType,
        containsBalancedBrackets: Boolean = false,
        fontStyle: FontStyle,
        foreground: UInt,
        background: UInt
    ): UInt {
        TODO()
    }

}

/**
 * Helpers to manage the "collapsed" metadata of an entire StackElement stack.
 * The following assumptions have been made:
 *  - languageId < 256 => needs 8 bits
 *  - unique color count < 512 => needs 9 bits
 *
 * The binary format is:
 * - -------------------------------------------
 *     3322 2222 2222 1111 1111 1100 0000 0000
 *     1098 7654 3210 9876 5432 1098 7654 3210
 * - -------------------------------------------
 *     xxxx xxxx xxxx xxxx xxxx xxxx xxxx xxxx
 *     bbbb bbbb ffff ffff fFFF FBTT LLLL LLLL
 * - -------------------------------------------
 *  - L = LanguageId (8 bits)
 *  - T = StandardTokenType (2 bits)
 *  - B = Balanced bracket (1 bit)
 *  - F = FontStyle (4 bits)
 *  - f = foreground color (9 bits)
 *  - b = background color (9 bits)
 */
object EncodedTokenDataConsts {
    const val LANGUAGEID_MASK = 0b00000000000000000000000011111111U
    const val TOKEN_TYPE_MASK = 0b00000000000000000000001100000000U
    const val BALANCED_BRACKETS_MASK = 0b00000000000000000000010000000000
    const val FONT_STYLE_MASK = 0b00000000000000000111100000000000
    const val FOREGROUND_MASK = 0b00000000111111111000000000000000
    const val BACKGROUND_MASK = 0b11111111000000000000000000000000

    const val LANGUAGEID_OFFSET = 0U
    const val TOKEN_TYPE_OFFSET = 8
    const val BALANCED_BRACKETS_OFFSET = 10
    const val FONT_STYLE_OFFSET = 11
    const val FOREGROUND_OFFSET = 15
    const val BACKGROUND_OFFSET = 24
}

typealias StandardTokenType = Byte

object StandardTokenTypeConsts {
    const val Other: StandardTokenType = 0
    const val Comment: StandardTokenType = 1
    const val String: StandardTokenType = 2
    const val RegEx: StandardTokenType = 3
}

typealias OptionalStandardTokenType = Byte

object OptionalStandardTokenTypeConsts {
    const val Other: OptionalStandardTokenType = 0
    const val Comment: OptionalStandardTokenType = 1
    const val String: OptionalStandardTokenType = 2
    const val RegEx: OptionalStandardTokenType = 3

    // Indicates that no token type is set.
    const val NotSet: OptionalStandardTokenType = 8
}

fun toOptionalTokenType(standardType: StandardTokenType): OptionalStandardTokenType {
    return standardType
}

private fun fromOptionalTokenType(standardType: OptionalStandardTokenType): StandardTokenType {
    return standardType
}