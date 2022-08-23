import kotlinx.serialization.Serializable
import org.codroid.textmate.EncodedTokenAttributes
import org.codroid.textmate.Registry
import org.codroid.textmate.SyncRegistry
import org.codroid.textmate.Tokenizer
import org.codroid.textmate.grammar.Grammar
import org.codroid.textmate.grammar.StateStack

@Serializable
data class ThemedToken(val content: String, val color: String)

@OptIn(ExperimentalUnsignedTypes::class)
fun tokenizeWithTheme(colorMap: Map<Int, String>, fileContents: String, grammar: Tokenizer): Array<ThemedToken> {
    val lines = fileContents.split(Regex("\r\n|\r|\n"))
    var ruleStack: StateStack? = null
    val actual = mutableListOf<ThemedToken>()
    for (line in lines) {
        val result = grammar.tokenizeLine2(line, ruleStack, 0)
        val tokenLength = result.tokens.size / 2
        for (j in 0 until tokenLength) {
            val startIndex = result.tokens[2 * j]
            val nextStartIndex = if (j + 1 < tokenLength) {
                result.tokens[2 * j + 2].toInt()
            } else {
                line.length
            }
            val tokenText = line.substring(startIndex.toInt(), nextStartIndex)
            if (tokenText.isEmpty()) continue

            val metadata = result.tokens[2 * j + 1]
            val foreground = EncodedTokenAttributes.getForeground(metadata)
            val foregroundColor = colorMap[foreground.toInt()]
            actual.add(ThemedToken(tokenText, foregroundColor!!.uppercase()))
        }
        ruleStack = result.ruleStack
    }
    return actual.toTypedArray()
}