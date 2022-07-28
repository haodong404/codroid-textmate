package org.codroid.textmate.theme

import org.codroid.textmate.utils.*
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * ThemeProvide
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/caab3de34a8cc7182141c9e31e0f42b96a3a1bac/src/grammar/grammar.ts" >
 *     src/grammar/grammar.ts</a>
 */
interface ThemeProvider {
    fun themeMatch(scopePath: ScopeStack): StyleAttributes?

    fun getDefaults(): StyleAttributes
}

class Theme(
    private val colorMap: ColorMap, private val defaults: StyleAttributes, private val root: ThemeTrieElement
) {
    companion object {
        fun createFromRawTheme(
            source: RawTheme? = null, colorMap: Array<String>? = null
        ): Theme = createFromParsedTheme(parseTheme(source), colorMap)

        fun createFromParsedTheme(
            source: MutableList<ParsedThemeRule>, colorMap: Array<String>? = null
        ): Theme = resolveParsedThemeRules(source, colorMap)
    }

    private val cachedMatchRoot = CachedFn<ScopeName, List<ThemeTrieElementRule>> {
        return@CachedFn this.root.match(it)
    }

    fun getColorMap(): Map<UInt, String> = colorMap.getColorMap()

    fun getDefaults(): StyleAttributes = defaults

    fun match(scopePath: ScopeStack?): StyleAttributes? {
        if (scopePath == null) return defaults
        val scopeName = scopePath.scopeName
        val matchingTrieElements = this.cachedMatchRoot.get(scopeName)
        val effectiveRule = matchingTrieElements.find {
            scopePathMatchesParentScopes(scopePath.parent, it.parentScopes)
        } ?: return null
        return StyleAttributes(
            effectiveRule.fontStyle, effectiveRule.foreground, effectiveRule.background
        )
    }

    override fun equals(other: Any?): Boolean {
        other?.let {
            if (other !is Theme) {
                return false
            }
            return root == other.root && colorMap == other.colorMap && defaults == other.defaults
        }
        return false
    }

    override fun hashCode(): Int {
        var result = colorMap.hashCode()
        result = 31 * result + defaults.hashCode()
        result = 31 * result + root.hashCode()
        result = 31 * result + cachedMatchRoot.hashCode()
        return result
    }

    override fun toString(): String {
        return "Theme(colorMap=$colorMap, defaults=$defaults, root=$root, cachedMatchRoot=$cachedMatchRoot)"
    }

}

/**
 * Identifiers with a binary dot operator.
 * Examples: `baz` or `foo.bar`
 */
typealias ScopeName = String

/**
 * An expression language of ScopeNames with a binary space (to indicate nesting) operator.
 * Examples: `foo.bar boo.baz`
 */
typealias ScopePath = String

/**
 * An expression language of ScopePathStr with a binary comma (to indicate alternatives) operator.
 * Examples: `foo.bar boo.baz,quick quack`
 */
typealias ScopePattern = String

private fun scopePathMatchesParentScopes(
    scopePath: ScopeStack?, parentScopes: List<ScopeName>?
): Boolean {
    if (parentScopes == null) return true
    var index = 0
    var scopePattern = parentScopes[index]
    var scopePathClone = scopePath?.clone()
    while (scopePathClone != null) {
        if (matchesScope(scopePathClone.scopeName, scopePattern)) {
            index++
            if (index == parentScopes.size) return true
            scopePattern = parentScopes[index]
        }
        scopePathClone = scopePathClone.parent
    }
    return false
}

private fun matchesScope(
    scopeName: ScopeName, scopePattern: ScopeName
): Boolean = scopePattern == scopeName || (scopeName.startsWith(scopePattern) && scopeName[scopePattern.length] == '.')

/**
 * Parse a raw theme into rules.
 */
fun parseTheme(source: RawTheme?): MutableList<ParsedThemeRule> {
    if (source == null) return mutableListOf()
    val settings = source.settings
    val result = mutableListOf<ParsedThemeRule>()
    for ((idx, setting) in settings.withIndex()) {
        val scopes = mutableListOf<String>()
        if (setting.scope != null) {
            val scope = StringBuilder(setting.scope.trim())
            if (scope.startsWith(',')) {   // remove leading commas
                scope.deleteAt(0)
            }
            if (scope.endsWith(',')) {  // remove trailing commas
                scope.deleteAt(scope.length - 1)
            }
            scopes.addAll(scope.split(','))
        } else if (setting.scopes != null) {
            scopes.addAll(setting.scopes)
        } else {
            scopes.add("")
        }

        var fontStyle = FontStyleConsts.NotSet
        setting.settings.fontStyle?.let {
            fontStyle = FontStyleConsts.None
            val segments = setting.settings.fontStyle.split(' ')
            for (seg in segments) {
                when (seg) {
                    "italic" -> fontStyle = fontStyle or FontStyleConsts.Italic
                    "bold" -> fontStyle = fontStyle or FontStyleConsts.Bold
                    "underline" -> fontStyle = fontStyle or FontStyleConsts.Underline
                    "strikethrough" -> fontStyle = fontStyle or FontStyleConsts.Strikethrough
                }
            }
        }
        var foreground: String? = null
        if (isValidHexColor(setting.settings.foreground ?: "")) {
            foreground = setting.settings.foreground
        }
        var background: String? = null
        if (isValidHexColor(setting.settings.background ?: "")) {
            background = setting.settings.background
        }

        for (item in scopes) {
            val scopeNow = item.trim()
            val segments = scopeNow.split(" ")

            val scope = segments[segments.size - 1]
            var parentScopes: List<ScopeName>? = null
            if (segments.size > 1) {
                parentScopes = segments.slice(0 until (segments.size - 1)).reversed()
            }
            result.add(
                ParsedThemeRule(
                    scope, parentScopes, idx, fontStyle, foreground, background
                )
            )
        }
    }
    return result
}

fun fontStyleToString(fontStyle: FontStyle): String {
    if (fontStyle == FontStyleConsts.NotSet) return "not set"

    val style = StringBuilder()
    if (fontStyle.and(FontStyleConsts.Italic).toBoolean()) {
        style.append("italic ")
    }
    if (fontStyle.and(FontStyleConsts.Bold).toBoolean()) {
        style.append("bold ")
    }
    if (fontStyle.and(FontStyleConsts.Underline).toBoolean()) {
        style.append("underline ")
    }
    if (fontStyle.and(FontStyleConsts.Strikethrough).toBoolean()) {
        style.append("strikethrough ")
    }
    if (style.isEmpty()) {
        style.append("none")
    }
    return style.trim().toString()
}

/**
 * Resolve rules (i.e. inheritance).
 */
private fun resolveParsedThemeRules(
    parsedThemeRules: MutableList<ParsedThemeRule>, colorMap: Array<String>?
): Theme {
    // Sort rules lexicographically, and then by index if necessary
    parsedThemeRules.sortWith(Comparator { o1, o2 ->
        var r = strcmp(o1.scope, o2.scope)
        if (r != 0) return@Comparator r
        r = strLisCmp(o1.parentScopes, o2.parentScopes)
        if (r != 0) return@Comparator r
        return@Comparator o1.index - o2.index
    })

    // Determine defaults
    var defaultFontStyle = FontStyleConsts.None
    var defaultForeground = "#000000"
    var defaultBackground = "#ffffff"
    val parsedThemeRulesClone = LinkedList<ParsedThemeRule>()
    for (item in parsedThemeRules) {
        parsedThemeRulesClone.add(item)
    }
    while (parsedThemeRulesClone.isNotEmpty() && parsedThemeRulesClone.first.scope.isEmpty()) {
        val incomingDefaults = parsedThemeRulesClone.first
        parsedThemeRulesClone.removeFirst()
        if (incomingDefaults.fontStyle != FontStyleConsts.NotSet) {
            defaultFontStyle = incomingDefaults.fontStyle
        }
        incomingDefaults.foreground?.let { defaultForeground = it }
        incomingDefaults.background?.let { defaultBackground = it }
    }
    val colorMap1 = ColorMap(colorMap)
    val defaults = StyleAttributes(
        defaultFontStyle, colorMap1.getId(defaultForeground), colorMap1.getId(defaultBackground)
    )
    val root = ThemeTrieElement(
        ThemeTrieElementRule(
            0, null, FontStyleConsts.NotSet, 0u, 0u
        )
    )
    for (rule in parsedThemeRulesClone) {
        root.insert(
            0,
            rule.scope,
            rule.parentScopes,
            rule.fontStyle,
            colorMap1.getId(rule.foreground),
            colorMap1.getId(rule.background)
        )
    }
    return Theme(colorMap1, defaults, root)
}

typealias TrieChildrenMap = HashMap<String, ThemeTrieElement>