import com.dd.plist.NSDictionary
import com.dd.plist.NSNumber
import com.dd.plist.NSObject
import com.dd.plist.PropertyListParser
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.capturedKClass
import org.codroid.textmate.*
import org.codroid.textmate.grammar.RawGrammar
import org.codroid.textmate.theme.RawTheme
import org.codroid.textmate.theme.ScopeName
import kotlin.reflect.full.primaryConstructor
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@Serializable
data class Entity(
    val name: String,
    val weight: Float,
    val pos: Position,
    val tags: Map<String, Tag>,
    val previous: Array<Position>,
    val long: Long,
    val double: Double
)

@Serializable
data class Tag(val desc: String? = null, val id: Int, val show: Boolean)

@Serializable
data class Position(val x: Int, val y: Int = -1)

@Serializable
data class NullableEntity(
    val content: String? = null,
    val position: Position? = null,
    val int: Int? = null,
    val float: Float? = null,
    val double: Double? = null,
    val boolean: Boolean? = null,
    val long: Long? = null,
    val arr: Array<Tag>? = null
)

class ParseTest {

    @Test
    fun `Test NsObjectDecoder`() {
        val dict = NSDictionary.fromJavaObject(
            Entity(
                "Codroid", 12.3F, Position(1, 3),
                mapOf(Pair("Tag1", Tag("Content", 1, false)), Pair("Tag2", Tag(null, 2, true))),
                arrayOf(Position(2, 3), Position(10)), 1000L, 23.1
            )
        )
        val result = decodeFromNSObject<Entity>(dict)
        println(result)
        assertEquals("Codroid", result.name)
        assertEquals(12.3F, result.weight)
        assertEquals(1000L, result.long)
        assertEquals(23.1, result.double)
        assertEquals(Position(1, 3), result.pos)
        assertContentEquals(
            mapOf(Pair("Tag1", Tag("Content", 1, false)), Pair("Tag2", Tag(null, 2, true))).entries,
            result.tags.entries.asIterable()
        )
        assertContentEquals(arrayOf(Position(2, 3), Position(10)), result.previous)
    }

    @Test
    fun `Can NsObjectDecoder decode nullable element`() {
        val dict = NSDictionary.fromJavaObject(NullableEntity())
        val result = decodeFromNSObject<NullableEntity>(dict)
        assertEquals(NullableEntity(), result)
    }

    @Test
    fun `Parse PLIST to RawTheme`() {
        val input = {}.javaClass.getResourceAsStream("themes/Solarized-light.tmTheme")
        input?.let {
            val theme = parsePLIST<RawTheme>(input)
            assertEquals("Solarized (light)", theme.name)
            assertEquals("#586E75", theme.settings!![0].settings?.foreground)
        }
    }

    @OptIn(InternalSerializationApi::class)
    @Test
    fun `Parse PLIST to RawGrammar`() {

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