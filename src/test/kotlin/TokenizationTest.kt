import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import oniguruma.OnigLib
import org.codroid.textmate.*
import org.codroid.textmate.exceptions.TextMateException
import org.codroid.textmate.grammar.RawGrammar
import org.codroid.textmate.grammar.StateStack
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertContentEquals

@Serializable
data class RawTest(
    val desc: String,
    val grammars: Array<String>,
    val grammarPath: String? = null,
    val grammarScopeName: String? = null,
    val grammarInjections: Array<String>? = null,
    val lines: Array<RawTestLine>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawTest

        if (desc != other.desc) return false
        if (!grammars.contentEquals(other.grammars)) return false
        if (grammarPath != other.grammarPath) return false
        if (grammarScopeName != other.grammarScopeName) return false
        if (grammarInjections != null) {
            if (other.grammarInjections == null) return false
            if (!grammarInjections.contentEquals(other.grammarInjections)) return false
        } else if (other.grammarInjections != null) return false
        if (!lines.contentEquals(other.lines)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = desc.hashCode()
        result = 31 * result + grammars.contentHashCode()
        result = 31 * result + (grammarPath?.hashCode() ?: 0)
        result = 31 * result + (grammarScopeName?.hashCode() ?: 0)
        result = 31 * result + (grammarInjections?.contentHashCode() ?: 0)
        result = 31 * result + lines.contentHashCode()
        return result
    }
}

@Serializable
data class RawTestLine(val line: String, var tokens: Array<RawToken>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawTestLine

        if (line != other.line) return false
        if (!tokens.contentEquals(other.tokens)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = line.hashCode()
        result = 31 * result + tokens.contentHashCode()
        return result
    }
}

@Serializable
data class RawToken(val value: String, val scopes: Array<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawToken

        if (value != other.value) return false
        if (!scopes.contentEquals(other.scopes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + scopes.contentHashCode()
        return result
    }
}

class TokenizationTest {

    private fun assertLineTokenization(grammar: Tokenizer, testCase: RawTestLine, prevState: StateStack?): StateStack {
        val actual = grammar.tokenizeLine(testCase.line, prevState, 0)
        val actualTokens = actual.tokens.map {
            return@map RawToken(
                testCase.line.substring(it.startIndex, min(testCase.line.length, it.endIndex)),
                it.scopes
            )
        }.toTypedArray()
        if (testCase.line.isNotEmpty()) {
            testCase.tokens = testCase.tokens.filter {
                it.value.isNotEmpty()
            }.toTypedArray()
        }


        assertContentEquals(testCase.tokens, actualTokens, "Tokenizing line ${testCase.line}")
        return actual.ruleStack
    }

    private suspend fun assertTokenizationSuite(testLocation: String) {

        suspend fun performTest(test: RawTest) {
            var grammarScopeName = test.grammarScopeName
            val grammarByScope = mutableMapOf<String, RawGrammar>()

            for (grammarPath in test.grammars) {
                val path = "${Path(testLocation).parent.pathString}/$grammarPath"
                val content = {}.javaClass.getResourceAsStream(path)
                if (content != null) {
                    val rawGrammar = parseRawGrammar(content, path)
                    grammarByScope[rawGrammar.scopeName] = rawGrammar
                    if (grammarScopeName == null && grammarPath == test.grammarPath) {
                        grammarScopeName = rawGrammar.scopeName
                    }
                }
            }
            if (grammarScopeName == null) {
                throw TextMateException("I HAVE NO GRAMMAR FOR TEST")
            }

            val options = RegistryOptions(
                regexLib = OnigLib(),
                loadGrammar = { grammarByScope[it] },
                getInjections = {
                    if (it == grammarScopeName) {
                        test.grammarInjections
                    } else {
                        null
                    }
                }
            )
            val registry = Registry(options)
            val grammar: Tokenizer = registry.loadGrammar(grammarScopeName)
            var prevState: StateStack? = null
            for (line in test.lines) {
                prevState = assertLineTokenization(grammar, line, prevState)
            }
        }

        {}.javaClass.getResourceAsStream(testLocation)?.let {
            parseJson<Array<RawTest>>(it).forEach { test ->
                print("Tokenization Testing: ${test.desc}".padEnd(20))
                performTest(test)
                println("√")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Test first-mate, tests json`() = runTest {
        assertTokenizationSuite("first-mate/tests.json")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Test suite1, tests json`() = runTest {
        assertTokenizationSuite("suite1/tests.json")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Test suite1, while Tests json`() = runTest {
        assertTokenizationSuite("suite1/whileTests.json")
    }
}

