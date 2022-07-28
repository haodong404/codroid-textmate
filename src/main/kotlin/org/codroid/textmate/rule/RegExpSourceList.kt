package org.codroid.textmate.rule

import org.codroid.textmate.oniguruma.OnigLib

class RegExpSourceList(
    val items: MutableList<RegExpSource> = mutableListOf(),
    var hasAnchors: Boolean = false,
    var cached: CompiledRule? = null,
    val anchorCache: RegExpSourceListAnchorCache = RegExpSourceListAnchorCache()
) {
    fun dispose() {
        disposeCaches()
    }

    private fun disposeCaches() {
        if (this.cached != null) {
            this.cached!!.dispose()
            this.cached = null
        }
        if (this.anchorCache.a0g0 != null) {
            this.anchorCache.a0g0!!.dispose()
            this.anchorCache.a0g0 = null
        }
        if (this.anchorCache.a0g1 != null) {
            this.anchorCache.a0g1!!.dispose()
            this.anchorCache.a0g1 = null
        }
        if (this.anchorCache.a1g0 != null) {
            this.anchorCache.a1g0!!.dispose()
            this.anchorCache.a1g0 = null
        }
        if (this.anchorCache.a1g1 != null) {
            this.anchorCache.a1g1!!.dispose()
            this.anchorCache.a1g1 = null
        }
    }

    fun push(item: RegExpSource) {
        this.items.add(item)
        this.hasAnchors = this.hasAnchors || item.hasAnchor
    }

    fun unshift(item: RegExpSource) {
        this.items.add(0, item)
        this.hasAnchors = this.hasAnchors || item.hasAnchor
    }

    fun length(): Int = this.items.size

    fun setSource(index: Int, newSource: String) {
        if (!this.items[index].source.contentEquals(newSource)) {
            // bust the cache
            this.disposeCaches()
            this.items[index].source = newSource
        }
    }

    fun compile(onigLib: OnigLib): CompiledRule {
        if (this.cached == null) {
            val regExps = this.items.map { it.source }
            this.cached = CompiledRule(onigLib, regExps.toTypedArray(), this.items.map { it.ruleId }.toTypedArray())
        }
        return this.cached!!
    }

    fun compileAG(onigLib: OnigLib, allowA: Boolean, allowG: Boolean): CompiledRule {
        if (!this.hasAnchors) {
            return this.compile(onigLib)
        } else {
            if (allowA) {
                if (allowG) {
                    if (this.anchorCache.a1g1 == null) {
                        this.anchorCache.a1g1 = this.resolveAnchors(onigLib, allowA, allowG);
                    }
                    return this.anchorCache.a1g1!!;
                } else {
                    if (this.anchorCache.a1g0 == null) {
                        this.anchorCache.a1g0 = this.resolveAnchors(onigLib, allowA, allowG);
                    }
                    return this.anchorCache.a1g0!!;
                }
            } else {
                if (allowG) {
                    if (this.anchorCache.a0g1 == null) {
                        this.anchorCache.a0g1 = this.resolveAnchors(onigLib, allowA, allowG);
                    }
                    return this.anchorCache.a0g1!!;
                } else {
                    if (this.anchorCache.a0g0 == null) {
                        this.anchorCache.a0g0 = this.resolveAnchors(onigLib, allowA, allowG);
                    }
                    return this.anchorCache.a0g0!!;
                }
            }
        }
    }

    private fun resolveAnchors(onigLib: OnigLib, allowA: Boolean, allowG: Boolean): CompiledRule {
        this.items.map { it.resolveAnchors(allowA, allowG) }.let {
            return CompiledRule(onigLib, it.toTypedArray(), this.items.map { e -> e.ruleId }.toTypedArray())
        }
    }
}