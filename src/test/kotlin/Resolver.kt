import kotlinx.serialization.Serializable
import org.codroid.textmate.RegistryOptions
import org.codroid.textmate.exceptions.TextMateException
import org.codroid.textmate.grammar.RawGrammar
import org.codroid.textmate.oniguruma.OnigLib
import org.codroid.textmate.parseRawGrammar
import org.codroid.textmate.regex.RegexLib
import org.codroid.textmate.theme.ScopeName

@Serializable
data class LanguageRegistration(
    val id: String = "",
    val extensions: Array<String> = emptyArray(),
    val filenames: Array<String> = emptyArray()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LanguageRegistration

        if (id != other.id) return false
        if (!extensions.contentEquals(other.extensions)) return false
        if (!filenames.contentEquals(other.filenames)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + extensions.contentHashCode()
        result = 31 * result + filenames.contentHashCode()
        return result
    }
}

@Serializable
data class GrammarRegistration(
    val language: String = "",
    val scopeName: ScopeName = "",
    var path: String = "",
    val embeddedLanguages: Map<String, String> = emptyMap(),
    var grammar: RawGrammar? = null
)

class Resolver(
    override val regexLib: RegexLib,
    val grammars: Array<GrammarRegistration>, val languages: Array<LanguageRegistration>
) : RegistryOptions(regexLib) {
    val language2id = mutableMapOf<String, Int>()
    val id2language = mutableMapOf<Int, String>()
    private var lastLanguageId = 0

    init {
        for (lang in this.languages) {
            val languageId = ++this.lastLanguageId
            this.language2id[lang.id] = languageId
            this.id2language[languageId] = lang.id
        }
    }

    fun findLanguageByExtension(fileExtension: String): String? {
        for (lang in this.languages) {
            if (lang.extensions.isEmpty()) continue
            for (extension in lang.extensions) {
                if (extension == fileExtension) {
                    return lang.id
                }
            }
        }
        return null
    }

    fun findLanguageByFilename(filename: String): String? {
        for (lan in this.languages) {
            if (lan.filenames.isEmpty()) continue
            for (filename_ in lan.filenames) {
                if (filename == filename_) {
                    return lan.id
                }
            }
        }
        return null
    }

    fun findGrammarByLanguage(language: String): GrammarRegistration {
        for (grammar in grammars) {
            if (grammar.language == language) {
                return grammar
            }
        }
        throw TextMateException("Could not findGrammarByLanguage for $language")
    }

    fun loadGrammar(scopeName: ScopeName): RawGrammar? {
        for (grammar in grammars) {
            if (grammar.scopeName == scopeName) {
                if (grammar.grammar == null) {
                    grammar.grammar = readGrammarFromPath(grammar.path)
                }
                return grammar.grammar
            }
        }
        return null
    }
}

fun readGrammarFromPath(path: String): RawGrammar {
    val content = {}.javaClass.getResourceAsStream(path)
    content?.let {
        return parseRawGrammar(it, path)
    }
    throw TextMateException("Not found.")
}