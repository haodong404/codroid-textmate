package org.codroid.textmate.grammar

import org.codroid.textmate.rule.RuleId
import org.codroid.textmate.theme.ScopeName

data class Location(val filename: String, val line: Int, val char: Int)

abstract class Locatable(open val location: Location? = null)

data class RawGrammar(
    override val location: Location? = null,

    val repository: RawRepository,
    val scopeName: ScopeName,
    val patterns: Array<RawRule>,
    val injections: HashMap<String, RawRule>? = null,
    val injectionSelector: String? = null,

    val fileTypes: Array<String>? = null,
    val name: String? = null,
    val firstLineMatch: String? = null,
) : Locatable(location), Cloneable {
    fun toRule(): RawRule = RawRule(
        location = this.location,
        name = this.name,
        patterns = this.patterns,
        repository = this.repository
    )

    public override fun clone(): RawGrammar = RawGrammar(
        location, repository, scopeName, patterns, injections, injectionSelector, fileTypes, name, firstLineMatch
    )
}

// String: name
typealias RawRepositoryMap = HashMap<String, RawRule>

data class RawRepository(
    override var location: Location? = null,
    var map: RawRepositoryMap? = null
) : Locatable(location)

data class RawRule(
    override val location: Location? = null,

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
    val while_: String? = null,
    val whileCaptures: RawCaptures? = null,
    var patterns: Array<RawRule>? = null,

    val repository: RawRepository? = null,
    val applyEndPatternLast: Boolean? = null
) : Locatable(location)

// String: captureId
typealias RawCapturesMap = HashMap<String, RawRule>

class RawCaptures(
    override val location: Location? = null,

    val map: RawCapturesMap? = null
) : Locatable(location)