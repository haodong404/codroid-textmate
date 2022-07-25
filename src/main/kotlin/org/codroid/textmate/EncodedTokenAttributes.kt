package org.codroid.textmate

import org.codroid.textmate.theme.FontStyle
import org.codroid.textmate.theme.FontStyleConsts

typealias EncodedToken = UInt

object EncodedTokenAttributes {
    fun toBinaryStr(encodedTokenAttributes: EncodedToken): String {
        var r = encodedTokenAttributes.toString(2)
        r = "0".repeat(32 - r.length) + r
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

    private infix fun UInt.ushr(diff: Int): UInt {
        return (this.toInt() ushr diff).toUInt()
    }

    fun getLanguageId(encodedTokenAttributes: EncodedToken): UInt {
        return (encodedTokenAttributes and EncodedTokenDataConsts.LANGUAGEID_MASK) ushr
                EncodedTokenDataConsts.LANGUAGEID_OFFSET
    }

    fun getTokenType(encodedTokenAttributes: EncodedToken): StandardTokenType {
        return ((encodedTokenAttributes and EncodedTokenDataConsts.TOKEN_TYPE_MASK) ushr
                EncodedTokenDataConsts.TOKEN_TYPE_OFFSET).toByte()
    }

    fun containsBalancedBrackets(encodedTokenAttributes: EncodedToken): Boolean {
        return (encodedTokenAttributes and EncodedTokenDataConsts.BALANCED_BRACKETS_MASK) != 0u;
    }

    fun getFontStyle(encodedTokenAttributes: EncodedToken): FontStyle {
        return ((encodedTokenAttributes and EncodedTokenDataConsts.FONT_STYLE_MASK) ushr
                EncodedTokenDataConsts.FONT_STYLE_OFFSET).toByte()
    }

    fun getForeground(encodedTokenAttributes: EncodedToken): UInt {
        return (encodedTokenAttributes and EncodedTokenDataConsts.FOREGROUND_MASK) ushr
                EncodedTokenDataConsts.FOREGROUND_OFFSET
    }

    fun getBackground(encodedTokenAttributes: EncodedToken): UInt {
        return (encodedTokenAttributes and EncodedTokenDataConsts.BACKGROUND_MASK) ushr
                EncodedTokenDataConsts.BACKGROUND_OFFSET
    }

    fun set(
        encodedTokenAttributes: EncodedToken,
        languageId: UInt,
        tokenType: OptionalStandardTokenType,
        containsBalancedBrackets: Boolean?,
        fontStyle: FontStyle,
        foreground: UInt,
        background: UInt
    ): UInt {
        var languageIdInit = getLanguageId(encodedTokenAttributes);
        var tokenTypeInit = getTokenType(encodedTokenAttributes);
        var containsBalancedBracketsBitInit: Int = if (containsBalancedBrackets(encodedTokenAttributes)) 1 else 0;
        var fontStyleInit = getFontStyle(encodedTokenAttributes);
        var foregroundInit = getForeground(encodedTokenAttributes);
        var backgroundInit = getBackground(encodedTokenAttributes);

        if (languageId != 0u) {
            languageIdInit = languageId;
        }
        if (tokenType != OptionalStandardTokenTypeConsts.NotSet) {
            tokenTypeInit = fromOptionalTokenType(tokenType);
        }

        containsBalancedBrackets?.let {
            containsBalancedBracketsBitInit = if (it) 1 else 0
        }

        if (fontStyle != FontStyleConsts.NotSet) {
            fontStyleInit = fontStyle;
        }
        if (foreground != 0u) {
            foregroundInit = foreground;
        }
        if (background != 0u) {
            backgroundInit = background;
        }

        return (
                ((languageIdInit shl EncodedTokenDataConsts.LANGUAGEID_OFFSET) or
                        (tokenTypeInit.toUInt() shl EncodedTokenDataConsts.TOKEN_TYPE_OFFSET) or
                        (containsBalancedBracketsBitInit shl
                                EncodedTokenDataConsts.BALANCED_BRACKETS_OFFSET).toUInt() or
                        (fontStyleInit.toUInt() shl EncodedTokenDataConsts.FONT_STYLE_OFFSET) or
                        (foregroundInit shl EncodedTokenDataConsts.FOREGROUND_OFFSET) or
                        (backgroundInit shl EncodedTokenDataConsts.BACKGROUND_OFFSET)) ushr
                        0
                );
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
    const val LANGUAGEID_MASK = 0b00000000000000000000000011111111u
    const val TOKEN_TYPE_MASK = 0b00000000000000000000001100000000u
    const val BALANCED_BRACKETS_MASK = 0b00000000000000000000010000000000u
    const val FONT_STYLE_MASK = 0b00000000000000000111100000000000u
    const val FOREGROUND_MASK = 0b00000000111111111000000000000000u
    const val BACKGROUND_MASK = 0b11111111000000000000000000000000u

    const val LANGUAGEID_OFFSET = 0
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