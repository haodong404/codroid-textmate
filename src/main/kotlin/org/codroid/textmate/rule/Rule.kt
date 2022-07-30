package org.codroid.textmate.rule

import kotlinx.serialization.Serializable
import org.codroid.textmate.RegexSource
import org.codroid.textmate.basename
import org.codroid.textmate.grammar.Location
import org.codroid.textmate.oniguruma.OnigCaptureIndex

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

    operator fun inc(): RuleId {
        this.id++
        return this
    }

    public override fun clone(): RuleId {
        return RuleId.from(this.id)
    }
}

interface WithPatternRule {
    val patterns: Array<RuleId>
    val hasMissingPatterns: Boolean
}

abstract class Rule(
    open val location: Location? = null,
    open val id: RuleId,
    open val name: String? = null,
    open val contentName: String? = null
) {
    private val nameIsCapturing = RegexSource.hasCaptures(name)
    private val contentNameIsCapturing = RegexSource.hasCaptures(contentName)

    abstract fun dispose()

    fun debugName(): String {
        val locate =
            if (this.location != null) "${basename(this.location!!.filename)}:${this.location!!.line}" else "unknown"
        return "${this.name}#${this.id} @ $locate"
    }

    fun getName(lineText: String?, captureIndices: Array<OnigCaptureIndex>?): String? {
        if (!this.nameIsCapturing || this.name == null || lineText == null || captureIndices == null) {
            return this.name
        }
        return RegexSource.replaceCaptures(this.name!!, lineText, captureIndices)
    }

    fun getContentName(lineText: String, captureIndices: Array<OnigCaptureIndex>): String? {
        if (!this.contentNameIsCapturing || this.contentName == null) {
            return this.contentName
        }
        return RegexSource.replaceCaptures(this.contentName!!, lineText, captureIndices)
    }

    abstract fun collectPatterns(grammar: RuleRegistry, out: RegExpSourceList)

    abstract fun compile(grammar: RuleRegistryOnigLib, endRegexSource: String): CompiledRule

    abstract fun compileAG(
        grammar: RuleRegistryOnigLib,
        endRegexSource: String,
        allowA: Boolean,
        allowG: Boolean
    ): CompiledRule
}