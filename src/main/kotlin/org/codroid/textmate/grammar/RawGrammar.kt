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
    var repository: RawRepository = RawRepository(),
    var scopeName: ScopeName = "",
    var patterns: Array<RawRule> = arrayOf(),
    var injections: HashMap<String, RawRule>? = null,
    var injectionSelector: String? = null,

    var fileTypes: Array<String>? = null,
    var name: String? = null,
    var firstLineMatch: String? = null,
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

    var include: String? = null,

    var name: String? = null,
    var contentName: String? = null,

    var match: String? = null,

    var captures: RawCaptures? = null,
    var begin: String? = null,

    var beginCaptures: RawCaptures? = null,
    var end: String? = null,

    var endCaptures: RawCaptures? = null,

    @SerialName("while")
    var while_: String? = null,

    var whileCaptures: RawCaptures? = null,

    var patterns: Array<RawRule>? = null,

    var repository: RawRepository? = null,

    /**
     * There is a bug when converting PLIST file.
     * 'applyEndPatternLast' is sometimes boolean and sometimes number.
     * <a href="https://github.com/3breadt/dd-plist">3breadt/dd-plist<a/> cannot process it well, but json can.
     */
    @Serializable(with = IntBooleanSerializer::class)
    var applyEndPatternLast: Boolean? = null
)

// String: captureId
typealias RawCaptures = HashMap<String, RawRule>