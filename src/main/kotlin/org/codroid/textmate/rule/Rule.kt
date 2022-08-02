package org.codroid.textmate.rule

import kotlinx.serialization.Serializable
import org.codroid.textmate.RegexSource

val HAS_BACK_REFERENCES = Regex("""\\(\d+)""")
val BACK_REFERENCING_END = Regex("""\\(\d+)""")

@Serializable
class RuleId(var id: Int) : Cloneable {
    companion object {
        val End = RuleId(-1)
        val While = RuleId(-2)

        fun from(id: Int) = RuleId(id)
    }

    override fun equals(other: Any?): Boolean {
        if (other is RuleId) {
            return this.id == other.id
        }
        return false
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "ID: $id"
    }

    operator fun inc(): RuleId {
        this.id++
        return this
    }

    public override fun clone(): RuleId {
        return from(this.id)
    }
}

interface WithPatternRule {
    val patterns: Array<RuleId>
    val hasMissingPatterns: Boolean
}

abstract class Rule() {
    abstract val id: RuleId
    abstract val name: String?
    abstract val contentName: String?

    abstract val nameIsCapturing: Boolean
    abstract val contentNameIsCapturing: Boolean

    abstract fun dispose()

    fun debugName(): String {
        return "${this.name}#${this.id}"
    }

    fun getName(lineText: String?, captureIndices: Array<IntRange>?): String? {
        if (!this.nameIsCapturing || this.name == null || lineText == null || captureIndices == null) {
            return this.name
        }
        return RegexSource.replaceCaptures(this.name!!, lineText, captureIndices)
    }

    fun getContentName(lineText: String, captureIndices: Array<IntRange>): String? {
        if (!this.contentNameIsCapturing || this.contentName == null) {
            return this.contentName
        }
        return RegexSource.replaceCaptures(this.contentName!!, lineText, captureIndices)
    }

    abstract fun collectPatterns(grammar: RuleRegistry, out: RegExpSourceList)

    abstract fun compile(grammar: RuleRegistryRegexLib, endRegexSource: String): CompiledRule

    abstract fun compileAG(
        grammar: RuleRegistryRegexLib,
        endRegexSource: String,
        allowA: Boolean,
        allowG: Boolean
    ): CompiledRule
}