package org.codroid.textmate.theme

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

    override fun toString(): String {
        return "ThemeTrieElementRule(scopeDepth=$scopeDepth, parentScopes=$parentScopes, fontStyle=$fontStyle, foreground=$foreground, background=$background)"
    }


}