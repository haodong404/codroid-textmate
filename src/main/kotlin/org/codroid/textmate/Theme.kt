package org.codroid.textmate

import org.codroid.textmate.utils.*
import java.util.LinkedList
import kotlin.experimental.and
import kotlin.experimental.or

class Theme(
    private val colorMap: ColorMap, private val defaults: StyleAttributes, private val root: ThemeTrieElement
) {
    companion object {
        fun createFromRawTheme(
            source: RawTheme?, colorMap: Array<String>?
        ): Theme = createFromParsedTheme(parseTheme(source), colorMap)

        fun createFromParsedTheme(
            source: MutableList<ParsedThemeRule>, colorMap: Array<String>?
        ): Theme = resolveParsedThemeRules(source, colorMap)
    }

    private val cachedMatchRoot = CachedFn<ScopeName, List<ThemeTrieElementRule>> {
        return@CachedFn this.root.match(it)
    }

    fun getColorMap(): List<String> = colorMap.getColorMap()

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

data class RawTheme(val name: String?, val settings: Array<RawThemeSetting>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawTheme

        if (name != other.name) return false
        if (!settings.contentEquals(other.settings)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + settings.contentHashCode()
        return result
    }
}


data class Setting(
    val fontStyle: String, val foreground: String, val background: String
)

data class RawThemeSetting(
    val name: String, val scopes: Array<ScopePattern>?, val scope: ScopePattern?, val settings: Setting
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawThemeSetting

        if (name != other.name) return false
        if (!scope.contentEquals(other.scope)) return false
        if (settings != other.settings) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + scope.hashCode()
        result = 31 * result + settings.hashCode()
        return result
    }
}

class ScopeStack(
    val parent: ScopeStack?, val scopeName: ScopeName
) : Cloneable {
    companion object {
        fun from(vararg segments: ScopeName): ScopeStack? {
            var result: ScopeStack? = null
            for (seg in segments) {
                result = ScopeStack(result, seg)
            }
            return result
        }
    }

    fun push(scopeName: ScopeName): ScopeStack = ScopeStack(this, scopeName)

    fun getSegments(): List<ScopeName> {
        var item: ScopeStack? = this
        val result = mutableListOf<ScopeName>()
        while (item != null) {
            result.add(item.scopeName)
            item = item.parent
        }
        result.reverse()
        return result
    }

    public override fun clone(): ScopeStack {
        return ScopeStack(parent, scopeName)
    }

    override fun toString(): String = this.getSegments().joinToString(" ")
}

private fun scopePathMatchesParentScopes(
    scopePath: ScopeStack?, parentScopes: List<ScopeName>?
): Boolean {
    if (parentScopes == null) return true
    var index = 0
    var scopePattern = parentScopes[index]
    var scopePathClone = scopePath?.clone()
    while (scopePathClone != null) {
        if (matchesScope(scopePathClone.scopeName, scopePattern)) {
            index++;
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

data class StyleAttributes(
    val fontStyle: FontStyle, val foreground: UInt, val background: UInt
)

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
            var scope = setting.scope
            scope = scope.replace(Regex("^,+"), "")
            scope = scope.replace(Regex("/,+\$"), "")
            scopes.addAll(scope.split(','))
        } else if (setting.scopes != null) {
            scopes.addAll(setting.scopes)
        } else {
            scopes.add("")
        }

        var fontStyle = FontStyleConsts.NotSet
        if (setting.settings.fontStyle == "string") {
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
        if (isValidHexColor(setting.settings.foreground)) {
            foreground = setting.settings.foreground
        }
        var background: String? = null
        if (isValidHexColor(setting.settings.background)) {
            background = setting.settings.background
        }

        for (item in scopes) {
            var scopeNow = item.trim()
            val segments = item.split(" ")

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

data class ParsedThemeRule(
    val scope: ScopeName,
    val parentScopes: List<ScopeName>?,
    val index: Int,
    val fontStyle: FontStyle,
    val foreground: String?,
    val background: String?
)

typealias FontStyle = Byte

fun fontStyleToString(fontStyle: FontStyle): String {
    if (fontStyle == FontStyleConsts.NotSet) return "not set"

    var style = StringBuilder()
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
    while (parsedThemeRulesClone.isNotEmpty() && parsedThemeRulesClone[0].scope.isEmpty()) {
        val incomingDefaults = parsedThemeRulesClone.first
        parsedThemeRulesClone.removeFirst()
        if (incomingDefaults.fontStyle != FontStyleConsts.NotSet) {
            defaultFontStyle = incomingDefaults.fontStyle
        }
        incomingDefaults.foreground?.let { defaultForeground = it }
        incomingDefaults.background?.let { defaultBackground = it }
    }
    val colorMap = ColorMap(colorMap)
    val defaults = StyleAttributes(
        defaultFontStyle, colorMap.getId(defaultForeground).toUInt(),
        colorMap.getId(defaultBackground).toUInt()
    )
    val root = ThemeTrieElement(
        ThemeTrieElementRule(
            0, null,
            FontStyleConsts.NotSet, 0u, 0u
        )
    )
    for (rule in parsedThemeRules) {
        root.insert(
            0,
            rule.scope,
            rule.parentScopes,
            rule.fontStyle,
            colorMap.getId(rule.foreground).toUInt(),
            colorMap.getId(rule.background).toUInt()
        )
    }
    return Theme(colorMap, defaults, root)
}

class ColorMap(colorMap: Array<String>?) {
    private val isFrozen: Boolean
    private var lastColorId = 0
    private val id2color = mutableListOf<String>()
    private val color2id = mutableMapOf<String, Int>()

    init {
        if (colorMap != null) {
            isFrozen = true
            for ((idx, item) in colorMap.withIndex()) {
                color2id[item] = idx
                id2color[idx] = item
            }
        } else {
            isFrozen = false
        }
    }

    fun getId(color: String?): Int {
        if (color == null) return 0
        val colorUpper = color.uppercase()
        var value = color2id[colorUpper]
        if (value != null) {
            return value
        }
        if (this.isFrozen) {
            throw IllegalArgumentException("Missing color in color map - $color")
        }
        value = ++this.lastColorId
        color2id[color] = value
        id2color[value] = color
        return value
    }

    fun getColorMap(): List<String> = id2color
}

class ThemeTrieElementRule(
    var scopeDepth: Int,
    val parentScopes: List<ScopeName>?,
    var fontStyle: FontStyle,
    var foreground: UInt,
    var background: UInt
) : Cloneable {
    public override fun clone(): ThemeTrieElementRule = ThemeTrieElementRule(
        scopeDepth, parentScopes, fontStyle, foreground, background
    )

    fun acceptOverwrite(scopeDepth: Int, fontStyle: FontStyle, foreground: UInt, background: UInt) {
        if (this.scopeDepth > scopeDepth) {
            println("How did this happen?")
        } else {
            this.scopeDepth = scopeDepth
        }
        // console.log('TODO -> my depth: ' + this.scopeDepth + ', overwriting depth: ' + scopeDepth);
        if (fontStyle != FontStyleConsts.NotSet) {
            this.fontStyle = fontStyle
        }
        if (foreground != 0u) {
            this.foreground = foreground
        }
        if (background != 0u) {
            this.background = background
        }
    }
}

typealias TrieChildrenMap = HashMap<String, ThemeTrieElement>

class ThemeTrieElement(
    private val mainRule: ThemeTrieElementRule,
    val rulesWithParentScopes: MutableList<ThemeTrieElementRule> = arrayListOf(),
    private val children: TrieChildrenMap = TrieChildrenMap()
) {
    companion object {
        private fun sortBySpecificity(arr: MutableList<ThemeTrieElementRule>): List<ThemeTrieElementRule> {
            if (arr.size == 1) return arr
            arr.sortWith(::cmpBySpecificity)
            return arr
        }

        private fun cmpBySpecificity(a: ThemeTrieElementRule, b: ThemeTrieElementRule): Int {
            if (a.scopeDepth == b.scopeDepth) {
                a.parentScopes?.let { aa ->
                    b.parentScopes?.let { bb ->
                        if (aa.size == bb.size) {
                            for (i in aa.indices) {
                                val aLen = aa[i].length
                                val bLen = bb[i].length
                                if (aLen != bLen) {
                                    return bLen - aLen
                                }
                            }
                        } else {
                            return bb.size - aa.size
                        }
                    }
                }
                return 0
            }
            return b.scopeDepth - a.scopeDepth
        }
    }

    fun match(scope: ScopeName): List<ThemeTrieElementRule> {
        if (scope.isEmpty()) {
            this.rulesWithParentScopes.add(mainRule)
            return sortBySpecificity(rulesWithParentScopes)
        }
        val dotIndex = scope.indexOf('.')
        var head = ""
        var tail = ""
        if (dotIndex == -1) {
            head = scope
        } else {
            head = scope.substring(0, dotIndex)
            tail = scope.substring(dotIndex + 1)
        }

        if (this.children.containsKey(head)) {
            return this.children[head]!!.match(tail)
        }
        this.rulesWithParentScopes.add(mainRule)
        return sortBySpecificity(rulesWithParentScopes)
    }

    fun insert(
        scopeDepth: Int,
        scope: ScopeName,
        parentScopes: List<ScopeName>?,
        fontStyle: FontStyle,
        foreground: UInt,
        background: UInt
    ) {
        if (scope.isEmpty()) {
            doInsertHere(scopeDepth, parentScopes, fontStyle, foreground, background)
            return
        }

        val dotIndex = scope.indexOf(".")
        var head = ""
        var tail = ""
        if (dotIndex == -1) {
            head = scope
        } else {
            head = scope.substring(0, dotIndex)
            tail = scope.substring(dotIndex + 1)
        }

        val child: ThemeTrieElement
        if (children.containsKey(head)) {
            child = children[head]!!
        } else {
            child = ThemeTrieElement(mainRule.clone(), rulesWithParentScopes.clone())
            children[head] = child
        }

        child.insert(
            scopeDepth + 1, tail, parentScopes, fontStyle, foreground, background
        )
    }

    fun doInsertHere(
        scopeDepth: Int, parentScopes: List<ScopeName>?, fontStyle: FontStyle, foreground: UInt, background: UInt
    ) {
        if (parentScopes == null) {
            // Merge into the main rule
            mainRule.acceptOverwrite(scopeDepth, fontStyle, foreground, background);
            return;
        }
        // Try to merge into existing rule
        for (rule in rulesWithParentScopes) {
            if (strLisCmp(rule.parentScopes, parentScopes) == 0) {
                // bingo! => we get to merge this into an existing one
                rule.acceptOverwrite(scopeDepth, fontStyle, foreground, background)
                return
            }
        }

        // Must add a new rule
        var fontStyleCopy = fontStyle
        var foregroundCopy = foreground
        var backgroundCopy = background
        // Inherit from main rule
        if (fontStyle == FontStyleConsts.NotSet) {
            fontStyleCopy = mainRule.fontStyle
        }
        if (foreground == 0u) {
            foregroundCopy = mainRule.foreground
        }
        if (background == 0u) {
            backgroundCopy = mainRule.background
        }
        rulesWithParentScopes.add(
            ThemeTrieElementRule(
                scopeDepth,
                parentScopes,
                fontStyleCopy,
                foregroundCopy,
                backgroundCopy
            )
        )
    }
}

object FontStyleConsts {
    const val NotSet: FontStyle = -1
    const val None: FontStyle = 0
    const val Italic: FontStyle = 1
    const val Bold: FontStyle = 2
    const val Underline: FontStyle = 4
    const val Strikethrough: FontStyle = 8
}