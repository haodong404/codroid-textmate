package org.codroid.textmate.grammar

import org.codroid.textmate.RuleId
import org.codroid.textmate.ScopeName

data class Location(val filename: String, val line: Int, val char: Int)

abstract class Locatable(open val location: Location? = null)

data class RawGrammar(
    override val location: Location? = null,

    val repository: RawRepository,
    val scopeName: ScopeName,
    val patterns: Array<RawRule>,
    val injections: HashMap<String, RawRule>? = null,
    val injectionSelector: String?,

    val fileTypes: Array<String>?,
    val name: String,
    val firstLineMatch: String?,
) : Locatable(location)

// String: name
typealias RawRepositoryMap = HashMap<String, RawRule>

data class RawRepository(
    override val location: Location? = null,
    val map: RawRepositoryMap? = null
) : Locatable(location)

data class RawRule(
    override val location: Location? = null,

    val id: RuleId? = null,

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
    val patterns: Array<RawRule>? = null,

    val repository: RawRepository? = null,
    val applyEndPatternLast: Boolean? = null
) : Locatable(location)

// String: captureId
typealias RawCapturesMap = HashMap<String, RawRule>

class RawCaptures(
    override val location: Location? = null,

    val captureMap: RawRule? = null
) : Locatable(location)