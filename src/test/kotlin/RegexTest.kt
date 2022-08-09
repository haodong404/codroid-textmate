import oniguruma.OnigLib
import org.codroid.textmate.regex.RegexExp
import org.codroid.textmate.regex.RegexLib
import org.codroid.textmate.regex.StandardRegex
import org.codroid.textmate.regex.StandardRegexExp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegexTest {

    private fun testRegexExp(regexLib: RegexLib) {
        val regex1 = regexLib.compile("abc")
        assertTrue(regex1.containsMatchIn(regexLib.createString("123abc")))
        assertFalse(regex1.containsMatchIn(regexLib.createString("efg")))

        val regex2 = regexLib.compile("^(\\d{3})-(\\d{3,8})\$")
        val result2 = regex2.search(regexLib.createString("010-12345"))
        assertEquals("010-12345", result2?.value)
        assertEquals("010-12345", result2?.groups!![0].value)
        assertEquals("010", result2.groups[1].value)
        assertEquals("12345", result2.groups[2].value)

        assertEquals(3, result2.count)

        val origin = regexLib.createString("123abcdef")
        val replaced = regex1.replace(origin) {
            assertEquals("abc", it.value)
            assertEquals(1, it.count)
            assertEquals(IntRange(3, 5), it.range)
            "[replaced]"
        }
        assertEquals("123[replaced]def", replaced)
    }

    private fun testMultibyteChar(regexLib: RegexLib) {
        val pattern = regexLib.compile("(?<=').*?(?=')")
        val str = regexLib.createString("'\uD835\uDEAF'")
        assertEquals("\uD835\uDEAF", pattern.search(str)?.value)
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