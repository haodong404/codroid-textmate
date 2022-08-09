import oniguruma.OnigLib
import org.codroid.textmate.regex.RegexLib
import org.codroid.textmate.regex.StandardRegex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegexTest {

    private fun testRegexExp(regexLib: RegexLib) {
        val regex1 = regexLib.compile("abc")
        assertTrue(regex1.containsMatchIn("123abc"))
        assertFalse(regex1.containsMatchIn("efg"))

        val regex2 = regexLib.compile("^(\\d{3})-(\\d{3,8})\$")
        val result2 = regex2.search("010-12345")
        assertEquals("010-12345", result2?.value)
        assertEquals("010-12345", result2?.groups!![0].value)
        assertEquals("010", result2.groups[1].value)
        assertEquals("12345", result2.groups[2].value)

        assertEquals(3, result2.count)

        val replaced = regex1.replace("123abcdef") {
            assertEquals("abc", it.value)
            assertEquals(1, it.count)
            assertEquals(IntRange(3, 5), it.range)
            "[replaced]"
        }
        assertEquals("123[replaced]def", replaced)
    }

    private fun testMultibyteChar(regexLib: RegexLib) {
        val pattern = regexLib.compile("(?<=').*?(?=')")
        assertEquals("\uD835\uDEAF", pattern.search("'\uD835\uDEAF'")?.value)
    }


    @Test
    fun `Test StandardRegexExp`() {
        StandardRegex().run {
            testRegexExp(this)
            testMultibyteChar(this)
        }
    }

    @Test
    fun `Test OnigRegExp`() {
        OnigLib().run {
            testRegexExp(this)
            testMultibyteChar(this)
        }
    }
}