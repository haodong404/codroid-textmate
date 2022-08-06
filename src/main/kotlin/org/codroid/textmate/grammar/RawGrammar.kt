package org.codroid.textmate.grammar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.codroid.textmate.IntBooleanSerializer
import org.codroid.textmate.rule.RuleId
import org.codroid.textmate.theme.ScopeName

@Serializable
data class Location(val filename: String, val line: Int, val char: Int)

@Serializable
abstract class Locatable() {
    abstract val location: Location?
}

@Serializable
data class RawGrammar(
    val repository: RawRepository = RawRepository(),
    val scopeName: ScopeName = "",
    val patterns: Array<RawRule> = arrayOf(),
    val injections: HashMap<String, RawRule>? = null,
    val injectionSelector: String? = null,

    val fileTypes: Array<String>? = null,
    val name: String? = null,
    val firstLineMatch: String? = null,
) : Cloneable {
    fun toRule(): RawRule = RawRule(
        name = this.name,
        patterns = this.patterns,
        repository = this.repository
    )

    public override fun clone(): RawGrammar = RawGrammar(
        repository, scopeName, patterns, injections, injectionSelector, fileTypes, name, firstLineMatch
    )

}

// String: name
typealias RawRepository = HashMap<String, RawRule>

@Serializable
data class RawRule(
    var id: RuleId? = null,

    val include: String? = null,

    val name: String? = null,
    val contentName: String? = null,

    val match: String? = null,

    val captures: RawCaptures? = null,
    val begin: String? = null,

    val beginCaptures: RawCaptures? = null,
    val end: String? = null,

    val endCaptures: RawCaptures? = null,

    @SerialName("while")
    val while_: String? = null,

    val whileCaptures: RawCaptures? = null,

    val patterns: Array<RawRule>? = null,

    val repository: RawRepository? = null,

    /**
     * There is a bug when converting PLIST file.
     * 'applyEndPatternLast' is sometimes boolean and sometimes number.
     * <a href="https://github.com/3breadt/dd-plist">3breadt/dd-plist<a/> cannot process it well, but json can.
     */
    @Serializable(with = IntBooleanSerializer::class)
    val applyEndPatternLast: Boolean? = null
)

// String: captureId
typealias RawCaptures = HashMap<String, RawRule>