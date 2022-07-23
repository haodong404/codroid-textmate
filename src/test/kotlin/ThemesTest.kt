import org.codroid.textmate.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemesTest {
    @Test
    fun `Theme matching gives higher priority to deeper matches`() {
        val theme = Theme.createFromRawTheme(
            source = RawTheme(
                settings = arrayOf(
                    RawThemeSetting(settings = Setting(foreground = "#100000", background = "#200000")),
                    RawThemeSetting(
                        scope = "punctuation.definition.string.begin.html", settings = Setting(foreground = "#300000")
                    ),
                    RawThemeSetting(
                        scope = "meta.tag punctuation.definition.string", settings = Setting(foreground = "#400000")
                    )
                )
            )
        )
        val actual = theme.match(ScopeStack.from("punctuation.definition.string.begin.html"))
        val actualForeground = actual?.foregroundId!!
        assertEquals("#300000", theme.getColorMap()[actualForeground])
    }

    @Test
    fun `Theme matching gives higher priority to parent matches 1`() {
        val theme = Theme.createFromRawTheme(
            source = RawTheme(
                settings = arrayOf(
                    RawThemeSetting(settings = Setting(foreground = "#100000", background = "#200000")),
                    RawThemeSetting(
                        scope = "c a", settings = Setting(foreground = "#300000")
                    ),
                    RawThemeSetting(
                        scope = "d a.b", settings = Setting(foreground = "#400000")
                    ),
                    RawThemeSetting(
                        scope = "a", settings = Setting(foreground = "#500000")
                    )
                )
            )
        )
        val map = theme.getColorMap()
        val id = theme.match(ScopeStack.from("d", "a.b"))?.foregroundId!!
        assertEquals("#400000", map[id])
    }

    @Test
    fun `Theme matching gives higher priority to parent matches 2`() {
        val theme = Theme.createFromRawTheme(
            source = RawTheme(
                settings = arrayOf(
                    RawThemeSetting(settings = Setting(foreground = "#100000", background = "#200000")),
                    RawThemeSetting(
                        scope = "meta.tag entity", settings = Setting(foreground = "#300000")
                    ),
                    RawThemeSetting(
                        scope = "meta.selector.css entity.name.tag", settings = Setting(foreground = "#400000")
                    ),
                    RawThemeSetting(
                        scope = "entity", settings = Setting(foreground = "#500000")
                    )
                )
            )
        )
        val result = theme.match(
            ScopeStack.from(
                "text.html.cshtml",
                "meta.tag.structure.any.html",
                "entity.name.tag.structure.any.html",
            )
        )
        val colorMap = theme.getColorMap()
        val id = result?.foregroundId
        assertEquals("#300000", colorMap[id])
    }

    @Test
    fun `Theme matching can match`() {
        val theme = Theme.createFromRawTheme(
            source = RawTheme(
                settings = arrayOf(
                    RawThemeSetting(settings = Setting(foreground = "#F8F8F2", background = "#272822")),
                    RawThemeSetting(
                        scope = "source, something", settings = Setting(background = "#100000")
                    ),
                    RawThemeSetting(
                        scopes = arrayOf("bar", "baz"), settings = Setting(background = "#200000")
                    ),
                    RawThemeSetting(
                        scope = "source.css selector bar", settings = Setting(fontStyle = "bold")
                    ),
                    RawThemeSetting(
                        scope = "constant", settings = Setting(fontStyle = "italic", foreground = "#300000")
                    ),
                    RawThemeSetting(
                        scope = "constant.numeric", settings = Setting(foreground = "#400000")
                    ),
                    RawThemeSetting(
                        scope = "constant.numeric.hex", settings = Setting(fontStyle = "bold")
                    ),
                    RawThemeSetting(
                        scope = "constant.numeric.oct", settings = Setting(fontStyle = "bold italic underline")
                    ),
                    RawThemeSetting(
                        scope = "constant.numeric.dec", settings = Setting(fontStyle = "", foreground = "#500000")
                    ),
                    RawThemeSetting(
                        scope = "storage.object.bar", settings = Setting(fontStyle = "", foreground = "#600000")
                    ),
                )
            )
        )

        val map = theme.getColorMap()
        fun match(vararg paths: ScopeName): Setting? {
            val result = theme.match(ScopeStack.from(*paths)) ?: return null
            val obj = Setting(fontStyle = fontStyleToString(result.fontStyle))
            if (result.foregroundId != 0u) {
                obj.foreground = map[result.foregroundId]
            }
            if (result.backgroundId != 0u) {
                obj.background = map[result.backgroundId]
            }
            return obj
        }

        fun test(summary: String, actual: Setting?, expected: Setting) {
            print(summary.padEnd(16))
            assertEquals(expected, actual)
            println("√")
        }

        test("simpleMatch1", match("source"), Setting(background = "#100000", fontStyle = "not set"))
        test("simpleMatch2", match("source.ts"), Setting(background = "#100000", fontStyle = "not set"))
        test("simpleMatch3", match("source.tss"), Setting(background = "#100000", fontStyle = "not set"))
        test("simpleMatch4", match("something"), Setting(background = "#100000", fontStyle = "not set"))
        test("simpleMatch5", match("something.ts"), Setting(background = "#100000", fontStyle = "not set"))
        test("simpleMatch6", match("something.tss"), Setting(background = "#100000", fontStyle = "not set"))
        test("simpleMatch7", match("baz"), Setting(background = "#200000", fontStyle = "not set"))
        test("simpleMatch8", match("baz.ts"), Setting(background = "#200000", fontStyle = "not set"))
        test("simpleMatch9", match("baz.tss"), Setting(background = "#200000", fontStyle = "not set"))
        test("simpleMatch10", match("constant"), Setting(foreground = "#300000", fontStyle = "italic"))
        test("simpleMatch11", match("constant.string"), Setting(foreground = "#300000", fontStyle = "italic"))
        test("simpleMatch12", match("constant.hex"), Setting(foreground = "#300000", fontStyle = "italic"))
        test("simpleMatch13", match("constant.numeric"), Setting(foreground = "#400000", fontStyle = "italic"))
        test("simpleMatch14", match("constant.numeric.baz"), Setting(foreground = "#400000", fontStyle = "italic"))
        test("simpleMatch15", match("constant.numeric.hex"), Setting(foreground = "#400000", fontStyle = "bold"))
        test("simpleMatch16", match("constant.numeric.hex.baz"), Setting(foreground = "#400000", fontStyle = "bold"))
        test(
            "simpleMatch17",
            match("constant.numeric.oct"),
            Setting(foreground = "#400000", fontStyle = "italic bold underline")
        )
        test(
            "simpleMatch18",
            match("constant.numeric.oct.baz"),
            Setting(foreground = "#400000", fontStyle = "italic bold underline")
        )
        test("simpleMatch19", match("constant.numeric.dec"), Setting(foreground = "#500000", fontStyle = "none"))
        test("simpleMatch20", match("constant.numeric.dec.baz"), Setting(foreground = "#500000", fontStyle = "none"))
        test("simpleMatch21", match("storage.object.bar"), Setting(foreground = "#600000", fontStyle = "none"))
        test("simpleMatch22", match("storage.object.bar.baz"), Setting(foreground = "#600000", fontStyle = "none"))
        test("simpleMatch23", match("storage.object.bart"), Setting(fontStyle = "not set"))
        test("simpleMatch24", match("storage.object"), Setting(fontStyle = "not set"))
        test("simpleMatch25", match("storage"), Setting(fontStyle = "not set"))

        test("defaultMatch1", match(""), Setting(fontStyle = "not set"))
        test("defaultMatch2", match("bazz"), Setting(fontStyle = "not set"))
        test("defaultMatch3", match("asdfg"), Setting(fontStyle = "not set"))

        test("multiMatch1", match("bar"), Setting(background = "#200000", fontStyle = "not set"))
        test(
            "multiMatch2",
            match("source.css", "selector", "bar"),
            Setting(background = "#200000", fontStyle = "bold")
        )
    }

    @Test
    fun `Theme matching Microsoft vscode#23460`() {

    }

    @Test
    fun `Theme parsing can parse`() {

    }

    @Test
    fun `Theme resolving strcmp works`() {

    }

    @Test
    fun `Theme resolving strArrCmp works`() {

    }

    fun assertThemeEqual(actual: Theme, expected: Theme) {

    }

    @Test
    fun `Theme resolving always has defaults`() {

    }

    @Test
    fun `Theme resolving respects incoming defaults 1`() {

    }

    @Test
    fun `Theme resolving respects incoming defaults 2`() {

    }

    @Test
    fun `Theme resolving respects incoming defaults 3`() {

    }

    @Test
    fun `Theme resolving respects incoming defaults 4`() {

    }

    @Test
    fun `Theme resolving respects incoming defaults 5`() {

    }

    @Test
    fun `Theme resolving can merge incoming defaults`() {

    }

    @Test
    fun `Theme resolving defaults are inherited`() {

    }

    @Test
    fun `Theme resolving same rules get merged`() {

    }

    @Test
    fun `Theme resolving rules are inherited 1`() {

    }

    @Test
    fun `Theme resolving rules are inherited 2`() {

    }

    @Test
    fun `Theme resolving rules with parent scopes`() {

    }

    @Test
    fun `Theme resolving issue #38, ignores rules with invalid colors`() {

    }

    @Test
    fun `Theme resolving issue #35, Trailing comma in a tmTheme scope selector`() {

    }


}