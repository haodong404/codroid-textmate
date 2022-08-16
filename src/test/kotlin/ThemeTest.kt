import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.codroid.textmate.EmbeddedLanguagesMap
import org.codroid.textmate.exceptions.TextMateException
import java.io.File
import java.nio.charset.StandardCharsets

typealias ThemesTokens = MutableMap<String, Array<ThemedToken>>

class ThemeTest(
    testFile: File, themeData: Array<ThemeData>, resolver: Resolver
) {
    private val expectedFile = themeFile("tests/$testFile.result")
    private val tests = mutableListOf<SingleThemeTest>()
    val expected = normalizeNewLines(
        String(
            expectedFile?.inputStream()?.readAllBytes() ?: ByteArray(0),
            StandardCharsets.UTF_8
        )
    )
    val testName = testFile.name
    var actual: String? = ""

    companion object {
        fun normalizeNewLines(str: String): String {
            return str.split(Regex("\r\n|\n")).joinToString("\n")
        }
    }

    init {
        val testFileContents = testFile.readText(StandardCharsets.UTF_8)

        var language = resolver.findLanguageByExtension(".${testFile.extension}")
        if (language == null) {
            language = resolver.findLanguageByFilename(testFile.name)
        }
        if (language == null) {
            throw TextMateException("Could not determine language for $testFile")
        }
        val grammar = resolver.findGrammarByLanguage(language)
        val embeddedLanguages = EmbeddedLanguagesMap()
        if (grammar.embeddedLanguages.isNotEmpty()) {
            for (scopeName in grammar.embeddedLanguages.keys) {
                val temp = grammar.embeddedLanguages[scopeName]
                embeddedLanguages[scopeName] = resolver.language2id[temp ?: ""] ?: -1
            }
        }

        for (theme in themeData) {
            this.tests.add(
                SingleThemeTest(
                    theme,
                    testFileContents,
                    grammar.scopeName,
                    resolver.language2id[language]!!,
                    embeddedLanguages
                )
            )
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun evaluate() {
        this.tests.map { it.evaluate() }
        val actual: ThemesTokens = mutableMapOf()
        for (test in tests) {
            actual[test.themeData.themeName] = test.actual!!
        }
        this.actual = normalizeNewLines(
            Json { ignoreUnknownKeys = true }.encodeToString(
                MapSerializer(
                    String.serializer(),
                    ArraySerializer(ThemedToken.serializer())
                ), actual
            )
        )
    }

    fun writeExpected() {
        this.expectedFile?.let {
            if (!it.exists()) {
                it.createNewFile()
            }
            this.actual?.let { t -> it.writeText(t) }
        }
    }
}

class SingleThemeTest(
    val themeData: ThemeData,
    val contents: String,
    val initialScopeName: String,
    val initialLanguage: Int,
    val embeddedLanguages: EmbeddedLanguagesMap
) {
    var actual: Array<ThemedToken>? = null

    fun evaluate() {
        this.actual = this.tokenizeWithThemeAsync()
    }

    private fun tokenizeWithThemeAsync(): Array<ThemedToken> {
        val grammar =
            this.themeData.registry.loadGrammarWithEmbeddedLanguages(
                this.initialScopeName,
                this.initialLanguage,
                this.embeddedLanguages
            )
        return tokenizeWithTheme(this.themeData.registry.getColorMap(), this.contents, grammar)
    }
}