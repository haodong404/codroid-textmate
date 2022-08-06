import com.dd.plist.NSDictionary
import com.dd.plist.PropertyListParser
import kotlinx.serialization.*
import org.codroid.textmate.*
import org.codroid.textmate.grammar.RawGrammar
import org.codroid.textmate.rule.RuleId
import org.codroid.textmate.theme.RawTheme
import org.codroid.textmate.theme.ScopeName
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Serializable
data class Entity(
    val name: String,
    val weight: Float,
    val pos: Position,
    val tags: Map<String, Tag>,
    val previous: Array<Position>,
    val long: Long,
    val double: Double,
    val nullBool: Boolean? = null
)

@Serializable
data class Tag(val desc: String? = null, val id: Int, val show: Boolean)

@Serializable
data class Position(val x: Int, val y: Int = -1)

@Serializable
data class NullableEntity(
    val position: Position? = null,
    val arr: Array<Tag>? = null,
    val content: String? = null,
    val int: Int? = null,
    val float: Float? = null,
    val double: Double? = null,
    val boolean: Boolean? = null,
    val long: Long? = null
)

@Serializable
data class BooleanTest(val a: Boolean)

class ParseTest {

    @Test
    fun `Test NsObjectDecoder`() {
        val dict = NSDictionary.fromJavaObject(
            Entity(
                "Codroid", 12.3F, Position(1, 3),
                mapOf(Pair("Tag1", Tag("Content", 1, false)), Pair("Tag2", Tag(null, 2, true))),
                arrayOf(Position(2, 3), Position(10)), 1000L, 23.1, true
            )
        )
        val result = decodeFromNSObject<Entity>(dict)
        println(result)
        assertEquals("Codroid", result.name)
        assertEquals(12.3F, result.weight)
        assertEquals(1000L, result.long)
        assertEquals(23.1, result.double)
        assertEquals(true, result.nullBool)
        assertEquals(Position(1, 3), result.pos)
        assertContentEquals(
            mapOf(Pair("Tag1", Tag("Content", 1, false)), Pair("Tag2", Tag(null, 2, true))).entries,
            result.tags.entries.asIterable()
        )
        assertContentEquals(arrayOf(Position(2, 3), Position(10)), result.previous)
    }

    @Test
    fun `Can decode nullable element`() {
        val dict = NSDictionary.fromJavaObject(NullableEntity())
        val result = decodeFromNSObject<NullableEntity>(dict)
        assertEquals(NullableEntity(), result)
    }

    @Serializable
    data class ArrayWithPrimitive(
        val str: Array<String>,
        val int: Array<Int>,
        val long: Array<Long>? = null,
        val float: Array<Float>,
        val double: Array<Double>? = null,
        val bool: Array<Boolean>,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ArrayWithPrimitive

            if (!str.contentEquals(other.str)) return false
            if (!int.contentEquals(other.int)) return false
            if (long != null) {
                if (other.long == null) return false
                if (!long.contentEquals(other.long)) return false
            } else if (other.long != null) return false
            if (!float.contentEquals(other.float)) return false
            if (double != null) {
                if (other.double == null) return false
                if (!double.contentEquals(other.double)) return false
            } else if (other.double != null) return false
            if (!bool.contentEquals(other.bool)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = str.contentHashCode()
            result = 31 * result + int.contentHashCode()
            result = 31 * result + (long?.contentHashCode() ?: 0)
            result = 31 * result + float.contentHashCode()
            result = 31 * result + (double?.contentHashCode() ?: 0)
            result = 31 * result + bool.contentHashCode()
            return result
        }
    }

    @Serializable
    data class MapWithPrimitive(
        val str: Map<String, String>,
        val int: Map<String, Int>? = null,
        val long: Map<String, Long>,
        val float: Map<String, Float>? = null,
        val double: Map<String, Double>,
        val bool: Map<String, Boolean>,
    )

    @Test
    fun `Can decode array or map with primitive kind`() {
        val expectedArray = ArrayWithPrimitive(
            str = arrayOf("A", "B", "C"),
            int = arrayOf(1, 2, 3),
            long = arrayOf(4, 5, 6),
            float = arrayOf(1F, 2F, 3F),
            bool = arrayOf(true, false, true)
        )
        val arrDict = NSDictionary.fromJavaObject(expectedArray)
        val actualArray = decodeFromNSObject<ArrayWithPrimitive>(arrDict)
        assertEquals(expectedArray, actualArray)

        val expectedMap = MapWithPrimitive(
            str = mapOf(Pair("A", "a"), Pair("B", "b"), Pair("C", "c")),
            int = mapOf(Pair("D", 1), Pair("E", 2), Pair("F", 3)),
            long = mapOf(Pair("G", 4)),
            double = mapOf(Pair("J", 7.0), Pair("K", 8.0), Pair("L", 9.0)),
            bool = mapOf(Pair("M", true), Pair("N", false))
        )

        val mapDict = NSDictionary.fromJavaObject(expectedMap)
        val actualMap = decodeFromNSObject<MapWithPrimitive>(mapDict)
        assertEquals(expectedMap, actualMap)
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

    @Test
    fun `Parse PLIST to RawGrammar`() {
        {}.javaClass.getResourceAsStream("suite1/fixtures/markdown.plist")?.let {
            val result = parsePLIST<RawGrammar>(it)
            assertTrue(
                result.repository["inline"]!!.repository!!["italic"]!!.patterns!![0].applyEndPatternLast ?: false
            )
            assertEquals("Markdown", result.name)
            assertContentEquals(arrayOf("md", "mdown", "markdown", "markdn"), result.fileTypes)
            assertEquals("text.html.markdown", result.scopeName)
            assertEquals("#block", result.patterns[0].include)
            assertEquals(2, result.repository.size)
            assertEquals(10, result.repository["block"]!!.repository!!.size)
        };

        {}.javaClass.getResourceAsStream("suite1/fixtures/66.plist")?.let {
            val grammar = parsePLIST<RawGrammar>(it)
            assertEquals("text.test", grammar.scopeName)
            assertEquals(2, grammar.patterns.size)
            assertEquals("comment", grammar.patterns[0].name)
        }
    }

    @Serializable
    data class RawGrammarTest(val scopeName: ScopeName, val rule: Array<RawRuleTest> = arrayOf())

    @Serializable
    data class RawRuleTest(val ruleID: RuleId? = null, val rule: String? = null, val boolean: Boolean)

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