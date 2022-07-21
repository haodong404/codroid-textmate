import org.codroid.textmate.*
import org.codroid.textmate.EncodedTokenAttributes.set
import kotlin.experimental.or
import kotlin.test.Test
import kotlin.test.assertEquals


class GrammarTest {

    private fun assertEqual(
        metadata: UInt,
        languageId: UInt,
        tokenType: StandardTokenType,
        containsBalancedBrackets: Boolean,
        fontStyle: FontStyle,
        foreground: UInt,
        background: UInt,
    ) {
        assertEquals(languageId, EncodedTokenAttributes.getLanguageId(metadata))
        assertEquals(tokenType, EncodedTokenAttributes.getTokenType(metadata))
        assertEquals(containsBalancedBrackets, EncodedTokenAttributes.containsBalancedBrackets(metadata))
        assertEquals(fontStyle, EncodedTokenAttributes.getFontStyle(metadata))
        assertEquals(foreground, EncodedTokenAttributes.getForeground(metadata))
        assertEquals(background, EncodedTokenAttributes.getBackground(metadata))
    }

    @Test
    fun `StackElementMetadata works`() {
        val value = set(
            0u,
            1u,
            OptionalStandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u, 102u
        )
        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u,
            102u
        )
    }

    @Test
    fun `StackElementMetadata can overwrite languageId`() {
        var value = set(
            0u,
            1u,
            OptionalStandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u, 102u
        )

        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u,
            102u
        )

        value = set(
            value,
            2u,
            OptionalStandardTokenTypeConsts.NotSet,
            false,
            FontStyleConsts.NotSet,
            0u, 0u
        )
        assertEqual(
            value,
            2u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u,
            102u
        )
    }

    @Test
    fun `StackElementMetadata can overwrite tokenType`() {
        var value = set(
            0u,
            1u,
            OptionalStandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u, 102u
        )

        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u,
            102u
        )

        value = set(
            value,
            0u,
            OptionalStandardTokenTypeConsts.Comment,
            false,
            FontStyleConsts.NotSet,
            0u, 0u
        )
        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.Comment,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u,
            102u
        )
    }

    @Test
    fun `StackElementMetadata can overwrite font style`() {
        var value = set(
            0u,
            1u,
            OptionalStandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u, 102u
        )

        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u,
            102u
        )

        value = set(
            value,
            0u,
            OptionalStandardTokenTypeConsts.NotSet,
            false,
            FontStyleConsts.None,
            0u, 0u
        )
        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.None,
            101u,
            102u
        )
    }

    @Test
    fun `StackElementMetadata can overwrite font style with strikethrough`() {
        var value = set(
            0u,
            1u,
            OptionalStandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Strikethrough,
            101u, 102u
        )

        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Strikethrough,
            101u,
            102u
        )

        value = set(
            value,
            0u,
            OptionalStandardTokenTypeConsts.NotSet,
            false,
            FontStyleConsts.None,
            0u, 0u
        )

        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.None,
            101u,
            102u
        )
    }

    @Test
    fun `StackElementMetadata can overwrite foreground`() {
        var value = set(
            0u,
            1u,
            OptionalStandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u, 102u
        )

        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u,
            102u
        )

        value = set(
            value,
            0u,
            OptionalStandardTokenTypeConsts.NotSet,
            false,
            FontStyleConsts.NotSet,
            5u, 102u
        )
        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            5u,
            102u
        )
    }

    @Test
    fun `StackElementMetadata can overwrite background`() {
        var value = set(
            0u,
            1u,
            OptionalStandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u, 102u
        )

        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u,
            102u
        )

        value = set(
            value,
            0u,
            OptionalStandardTokenTypeConsts.NotSet,
            false,
            FontStyleConsts.NotSet,
            0u, 7u
        )
        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u,
            7u
        )
    }

    @Test
    fun `StackElementMetadata can overwrite balanced backet bit`() {
        var value = set(
            0u,
            1u,
            OptionalStandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u, 102u
        )

        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u,
            102u
        )

        value = set(
            value,
            0u,
            OptionalStandardTokenTypeConsts.NotSet,
            true,
            FontStyleConsts.NotSet,
            0u, 0u
        )
        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            true,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u,
            102u
        )

        value = set(
            value,
            0u,
            OptionalStandardTokenTypeConsts.NotSet,
            false,
            FontStyleConsts.NotSet,
            0u, 0u
        )

        assertEqual(
            value,
            1u,
            StandardTokenTypeConsts.RegEx,
            false,
            FontStyleConsts.Underline or FontStyleConsts.Bold,
            101u,
            102u
        )
    }

    @Test
    fun `StackElementMetadata can work at max values`() {
        val maxLangId = 255u
        val maxTokenType =
            StandardTokenTypeConsts.Comment or StandardTokenTypeConsts.Other or StandardTokenTypeConsts.RegEx or StandardTokenTypeConsts.String
        val maxFontStyle = FontStyleConsts.Bold or FontStyleConsts.Italic or FontStyleConsts.Underline
        val maxForeground = 511u
        val maxBackground = 254u

        val value = set(
            0u,
            maxLangId,
            maxTokenType,
            true,
            maxFontStyle,
            maxForeground, maxBackground
        )

        assertEqual(
            value,
            maxLangId,
            maxTokenType,
            true,
            maxFontStyle,
            maxForeground,
            maxBackground
        )
    }

    @Test
    fun `Shadowed rules are resolved correctly`() {
        TODO()
    }
}