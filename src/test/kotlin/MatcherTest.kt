import org.codroid.textmate.Matchers
import kotlin.test.Test
import kotlin.test.assertEquals

class MatcherTest {
    data class Case(val expression: String, val input: Array<String>, val result: Boolean)

    private val cases = arrayOf(
        Case("foo", arrayOf("foo"), true),
        Case("foo", arrayOf("bar"), false),
        Case("- foo", arrayOf("foo"), false),
        Case("- foo", arrayOf("bar"), true),
        Case("- - foo", arrayOf("bar"), false),
        Case("bar foo", arrayOf("foo"), false),
        Case("bar foo", arrayOf("bar"), false),
        Case("bar foo", arrayOf("bar", "foo"), true),
        Case("bar - foo", arrayOf("bar"), true),
        Case("bar - foo", arrayOf("foo", "bar"), false),
        Case("bar - foo", arrayOf("foo"), false),
        Case("bar, foo", arrayOf("foo"), true),
        Case("bar, foo", arrayOf("bar"), true),
        Case("bar, foo", arrayOf("bar", "foo"), true),
        Case("bar, -foo", arrayOf("bar", "foo"), true),
        Case("bar, -foo", arrayOf("yo"), true),
        Case("bar, -foo", arrayOf("foo"), false),
        Case("(foo)", arrayOf("foo"), true),
        Case("(foo - bar)", arrayOf("foo"), true),
        Case("(foo - bar)", arrayOf("foo", "bar"), false),
        Case("foo bar - (yo man)", arrayOf("foo", "bar"), true),
        Case("foo bar - (yo man)", arrayOf("foo", "bar", "yo"), true),
        Case("foo bar - (yo man)", arrayOf("foo", "bar", "yo", "man"), false),
        Case("foo bar - (yo | man)", arrayOf("foo", "bar", "yo", "man"), false),
        Case("foo bar - (yo | man)", arrayOf("foo", "bar", "yo"), false),
        Case("R:text.html - (comment.block, text.html source)", arrayOf("text.html", "bar", "source"), false),
        Case(
            "text.html.php - (meta.embedded | meta.tag), L:text.html.php meta.tag, L:source.js.embedded.html",
            arrayOf("text.html.php", "bar", "source.js"),
            true
        )
    )

    private fun nameMatcher(identifiers: List<String>, stackElements: Array<String>): Boolean {
        var lastIndex = 0
        identifiers.forEach {
            var temp = false
            for (i in lastIndex until stackElements.size) {
                if (stackElements[i] == it) {
                    lastIndex = i + 1
                    temp = true
                }
            }
            if (!temp) return false
        }
        return true
    }

    @Test
    fun `Matcher Tests`() {
        cases.forEachIndexed { index, it ->
            val matchers = Matchers.create(it.expression, this::nameMatcher)
            var result = false
            matchers.forEach inside@{ m ->
                if (m.matcher(it.input)) {
                    result = true
                    return@inside
                }
            }
            print("Matcher Test #$index".padEnd(20))
            assertEquals(it.result, result)
            print("√\n")
        }
    }
}