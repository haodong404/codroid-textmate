package org.codroid.textmate.grammar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.codroid.textmate.RawCapturesMapSerializer
import org.codroid.textmate.RawRepositorySerializer
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
    override val location: Location? = null,

    @Serializable(with = RawRepositorySerializer::class)
    val repository: RawRepository,
    val scopeName: ScopeName,
    val patterns: Array<RawRule>,
    val injections: HashMap<String, RawRule>? = null,
    val injectionSelector: String? = null,

    val fileTypes: Array<String>? = null,
    val name: String? = null,
    val firstLineMatch: String? = null,
) : Locatable(), Cloneable {
    fun toRule(): RawRule = RawRule(
        location = this.location,
        name = this.name,
        patterns = this.patterns,
        repository = this.repository
    )

    public override fun clone(): RawGrammar = RawGrammar(
        location, repository, scopeName, patterns, injections, injectionSelector, fileTypes, name, firstLineMatch
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawGrammar

        if (location != other.location) return false
        if (repository != other.repository) return false
        if (scopeName != other.scopeName) return false
        if (!patterns.contentEquals(other.patterns)) return false
        if (injections != other.injections) return false
        if (injectionSelector != other.injectionSelector) return false
        if (fileTypes != null) {
            if (other.fileTypes == null) return false
            if (!fileTypes.contentEquals(other.fileTypes)) return false
        } else if (other.fileTypes != null) return false
        if (name != other.name) return false
        if (firstLineMatch != other.firstLineMatch) return false

        return true
    }

    override fun hashCode(): Int {
        var result = location?.hashCode() ?: 0
        result = 31 * result + repository.hashCode()
        result = 31 * result + scopeName.hashCode()
        result = 31 * result + patterns.contentHashCode()
        result = 31 * result + (injections?.hashCode() ?: 0)
        result = 31 * result + (injectionSelector?.hashCode() ?: 0)
        result = 31 * result + (fileTypes?.contentHashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (firstLineMatch?.hashCode() ?: 0)
        return result
    }
}

// String: name
typealias RawRepositoryMap = HashMap<String, RawRule>

@Serializable
data class RawRepository(
    override var location: Location? = null,
    var map: RawRepositoryMap? = null
) : Locatable()

@Serializable
data class RawRule(
    override val location: Location? = null,

    var id: RuleId? = null,

    val include: String? = null,

    val name: String? = null,
    val contentName: String? = null,

    val match: String? = null,

    @Serializable(with = RawCapturesMapSerializer::class)
    val captures: RawCaptures? = null,
    val begin: String? = null,

    @Serializable(with = RawCapturesMapSerializer::class)
    val beginCaptures: RawCaptures? = null,
    val end: String? = null,

    @Serializable(with = RawCapturesMapSerializer::class)
    val endCaptures: RawCaptures? = null,

    @SerialName("while")
    val while_: String? = null,

    @Serializable(with = RawCapturesMapSerializer::class)
    val whileCaptures: RawCaptures? = null,
    var patterns: Array<RawRule>? = null,

    @Serializable(with = RawRepositorySerializer::class)
    val repository: RawRepository? = null,
    val applyEndPatternLast: Boolean? = null
) : Locatable()

// String: captureId
typealias RawCapturesMap = HashMap<String, RawRule>

@Serializable
class RawCaptures(
    override val location: Location? = null,
    val map: RawCapturesMap? = null
) : Locatable()