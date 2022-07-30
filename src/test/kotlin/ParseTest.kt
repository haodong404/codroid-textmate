import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import org.codroid.textmate.grammar.RawGrammar
import org.codroid.textmate.grammar.RawRepository
import org.codroid.textmate.parseJson
import org.codroid.textmate.parsePLIST
import org.codroid.textmate.theme.RawTheme
import org.codroid.textmate.theme.ScopeName
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class RawGrammarTest(
    val name: String, val scopeName: ScopeName,
    @Serializable(with = RepositorySerializer::class)
    val repository: RawRepository
)

data class RawRepository(val map: Map<String, RawRule>)

@Serializable
data class RawRule(val name: String? = null, val beginCaptures: Map<String, Captures>? = null)

@Serializable
data class Captures(val name: String? = null)

object RepositorySerializer : JsonTransformingSerializer<RawRepository>(RawRepository.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonObject) {
            return JsonObject(
                mapOf(
                    Pair("map", element)
                )
            )
        }
        return element
    }
}

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
            assertEquals("meta.block.cpp", syntax.repository.map!!["block"]!!.name)
            assertEquals(
                "support.function.any-method.c",
                syntax.repository.map?.get("block")?.patterns?.getOrNull(0)?.captures?.map?.get("1")?.name
            )
            assertEquals(
                "punctuation.definition.parameters.c",
                syntax.repository.map?.get("block")?.patterns?.getOrNull(0)?.captures?.map?.get("2")?.name
            )
        }
    }

}