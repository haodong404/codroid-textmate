package org.codroid.textmate.theme

class ThemeTrieElementRule(
    var scopeDepth: Int,
    val parentScopes: List<ScopeName>?,
    var fontStyle: FontStyle,
    var foreground: Int,
    var background: Int
) : Cloneable, Comparable<ThemeTrieElementRule> {
    public override fun clone(): ThemeTrieElementRule = ThemeTrieElementRule(
        scopeDepth, parentScopes?.toList(), fontStyle, foreground, background
    )

    fun acceptOverwrite(scopeDepth: Int, fontStyle: FontStyle, foreground: Int, background: Int) {
        if (this.scopeDepth > scopeDepth) {
            println("How did this happen?")
        } else {
            this.scopeDepth = scopeDepth
        }
        // console.log('TODO -> my depth: ' + this.scopeDepth + ', overwriting depth: ' + scopeDepth);
        if (fontStyle != FontStyleConsts.NotSet) {
            this.fontStyle = fontStyle
        }
        if (foreground != 0) {
            this.foreground = foreground
        }
        if (background != 0) {
            this.background = background
        }
    }

    override fun compareTo(other: ThemeTrieElementRule): Int {
        if (this.scopeDepth == other.scopeDepth) {
            val aParentScopes = this.parentScopes
            val bParentScopes = other.parentScopes
            val aParentScopesLen = aParentScopes?.size ?: 0
            val bParentScopesLen = bParentScopes?.size ?: 0
            if (aParentScopesLen == bParentScopesLen) {
                repeat(aParentScopesLen) {
                    val aLen = aParentScopes!![it].length
                    val bLen = bParentScopes!![it].length
                    if (aLen != bLen) {
                        return bLen - aLen
                    }
                }
            }
            return bParentScopesLen - aParentScopesLen
        }
        return other.scopeDepth - this.scopeDepth
    }

    override fun toString(): String {
        return "ThemeTrieElementRule(scopeDepth=$scopeDepth, parentScopes=$parentScopes, fontStyle=$fontStyle, foreground=$foreground, background=$background)"
    }


}