package org.codroid.textmate.rule

import org.codroid.textmate.escapeRegExpCharacters

class RegExpSource(regExpSource: String, var ruleId: RuleId) : Cloneable {
    var source: String = ""
        set(value) {
            if (field != value) {
                if (this.hasAnchor) {
                    this.anchorCache = this.buildAnchorCache()
                }
                field = value
            }
        }

    val hasAnchor: Boolean
    val hasBackReferences: Boolean
    private var anchorCache: RegExpSourceAnchorCache?

    init {
        if (regExpSource.isNotEmpty()) {
            var lastPushedPos = 0
            val output = mutableListOf<String>()

            var hasAnchor = false
            var idx = 0
            while (idx < regExpSource.length) {
                val char = regExpSource[idx]
                if (char == '\\') {
                    if (idx + 1 < regExpSource.length) {
                        val nextChar = regExpSource[idx + 1]
                        if (nextChar == 'z') {
                            output.add(regExpSource.substring(lastPushedPos, idx))
                            output.add("\$(?!\\n)(?<!\\n)")
                            lastPushedPos = idx + 2
                        } else if (nextChar == 'A' || nextChar == 'G') {
                            hasAnchor = true
                        }
                        idx++
                    }
                }
                idx++
            }

            this.hasAnchor = hasAnchor
            if (lastPushedPos == 0) {
                // No \z hit
                this.source = regExpSource
            } else {
                output.add(regExpSource.substring(lastPushedPos, regExpSource.length))
                this.source = output.joinToString("")
            }
        } else {
            this.hasAnchor = false
            this.source = regExpSource
        }

        if (this.hasAnchor) {
            this.anchorCache = this.buildAnchorCache()
        } else {
            this.anchorCache = null
        }
        this.hasBackReferences = HAS_BACK_REFERENCES.containsMatchIn(this.source)
    }

    public override fun clone(): RegExpSource {
        return RegExpSource(this.source, this.ruleId)
    }

    fun resolveBackReferences(lineText: String, captureIndices: Array<IntRange>): String {
        val captureValues = captureIndices.map {
            lineText.substring(it.first, it.last)
        }
        return this.source.replace(BACK_REFERENCING_END) {
            return@replace escapeRegExpCharacters(captureValues.getOrElse(it.groupValues[1].toInt()) { "" })
        }
    }

    fun buildAnchorCache(): RegExpSourceAnchorCache {
        val len = source.length
        val a0g0Result = CharArray(len)
        val a0g1Result = CharArray(len)
        val a1g0Result = CharArray(len)
        val a1g1Result = CharArray(len)

        var pos = 0
        var charNow: Char
        var nextChar: Char
        while (pos < len) {
            charNow = this.source[pos]
            a0g0Result[pos] = charNow
            a0g1Result[pos] = charNow
            a1g0Result[pos] = charNow
            a1g1Result[pos] = charNow
            if (charNow == '\\') {
                if (pos + 1 < len) {
                    nextChar = this.source[pos + 1]
                    when (nextChar) {
                        'A' -> {
                            a0g0Result[pos + 1] = '\uFFFF';
                            a0g1Result[pos + 1] = '\uFFFF';
                            a1g0Result[pos + 1] = 'A';
                            a1g1Result[pos + 1] = 'A';
                        }
                        'G' -> {
                            a0g0Result[pos + 1] = '\uFFFF';
                            a0g1Result[pos + 1] = 'G';
                            a1g0Result[pos + 1] = '\uFFFF';
                            a1g1Result[pos + 1] = 'G';
                        }
                        else -> {
                            a0g0Result[pos + 1] = nextChar;
                            a0g1Result[pos + 1] = nextChar;
                            a1g0Result[pos + 1] = nextChar;
                            a1g1Result[pos + 1] = nextChar;
                        }
                    }
                    pos++
                }
            }
            pos++
        }
        return RegExpSourceAnchorCache(
            a0g0 = String(a0g0Result),
            a0g1 = String(a0g1Result),
            a1g0 = String(a1g0Result),
            a1g1 = String(a1g1Result),
        )
    }

    fun resolveAnchors(allowA: Boolean, allowG: Boolean): String {
        if (!this.hasAnchor || this.anchorCache == null) {
            return this.source
        }
        return if (allowA) {
            if (allowG) {
                this.anchorCache!!.a1g1
            } else {
                this.anchorCache!!.a1g0
            }
        } else {
            if (allowG) {
                this.anchorCache!!.a0g1
            } else {
                this.anchorCache!!.a0g0
            }
        }
    }
}