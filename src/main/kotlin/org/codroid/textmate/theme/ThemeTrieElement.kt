package org.codroid.textmate.theme

import org.codroid.textmate.utils.clone
import org.codroid.textmate.utils.strLisCmp

class ThemeTrieElement(
    private val mainRule: ThemeTrieElementRule,
    private val rulesWithParentScopes: MutableList<ThemeTrieElementRule> = arrayListOf(),
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
        val head: String
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
        val head: String
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

    private fun doInsertHere(
        scopeDepth: Int, parentScopes: List<ScopeName>?, fontStyle: FontStyle, foreground: UInt, background: UInt
    ) {
        if (parentScopes == null) {
            // Merge into the main rule
            mainRule.acceptOverwrite(scopeDepth, fontStyle, foreground, background)
            return
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
                scopeDepth, parentScopes, fontStyleCopy, foregroundCopy, backgroundCopy
            )
        )
    }


    override fun equals(other: Any?): Boolean {
        other?.let {
            if (other !is ThemeTrieElement) {
                return false
            }
            return toString().contentEquals(other.toString())
        }
        return false
    }

    override fun hashCode(): Int {
        var result = mainRule.hashCode()
        result = 31 * result + rulesWithParentScopes.hashCode()
        result = 31 * result + children.hashCode()
        return result
    }

    override fun toString(): String {
        return "ThemeTrieElement(mainRule=$mainRule, rulesWithParentScopes=$rulesWithParentScopes, children=$children)"
    }

}