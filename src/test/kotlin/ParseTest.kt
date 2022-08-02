import com.dd.plist.NSDictionary
import kotlinx.serialization.Serializable
import org.codroid.textmate.IntBooleanSerializer
import org.codroid.textmate.grammar.RawGrammar
import org.codroid.textmate.parseJson
import org.codroid.textmate.parsePLIST
import org.codroid.textmate.theme.RawTheme
import org.codroid.textmate.theme.ScopeName
import kotlin.test.Test
import kotlin.test.assertEquals

data class RawGrammarTest(
    var name: String? = null, var scopeName: ScopeName = "",
    var repository: NSDictionary? = null
//    val repository: RawRepository? = null
)

data class RawRepository(val map: Map<String, RawRule>)

@Serializable
data class RawRule(val name: String? = null, val beginCaptures: Map<String, Captures>? = null)

@Serializable
data class Captures(val name: String? = null)

class ParseTest {

    @Test
    fun `Parse PLIST to RawTheme`() {
        val input = {}.javaClass.getResourceAsStream("themes/Solarized-light.tmTheme")
        input?.let {
            val theme = parsePLIST<RawTheme>(input)
            assertEquals("Solarized (light)", theme.name)
            assertEquals("#586E75", theme.settings!![0].settings?.foreground)
        }
    }

    @Test
    fun `Parse Json to RawTheme`() {
        val input = {}.javaClass.getResourceAsStream("themes/syntaxes/c++.json")
        input?.let {
            val syntax = parseJson<RawGrammar>(input)
            assertEquals("C++", syntax.name)
            assertEquals("source.cpp", syntax.scopeName)
            assertEquals("meta.block.cpp", syntax.repository["block"]!!.name)
            assertEquals(
                "support.function.any-method.c",
                syntax.repository["block"]?.patterns?.getOrNull(0)?.captures?.get("1")?.name
            )
            assertEquals(
                "punctuation.definition.parameters.c",
                syntax.repository["block"]?.patterns?.getOrNull(0)?.captures?.get("2")?.name
            )
        }
    }

}