package org.codroid.textmate.rule

import org.codroid.textmate.grammar.Location

class MatchRule(
    override val location: Location?,
    override val id: RuleId,
    override val name: String?,
    override val contentName: String?,

    match: String? = null,
    val captures: Array<CaptureRule?>
) : Rule(location, id, name, contentName) {
    private var cachedCompiledPatterns: RegExpSourceList?
    private val match = RegExpSource(match, this.id)

    init {

    }
}