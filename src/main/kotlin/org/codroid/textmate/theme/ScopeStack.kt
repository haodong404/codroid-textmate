package org.codroid.textmate.theme

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