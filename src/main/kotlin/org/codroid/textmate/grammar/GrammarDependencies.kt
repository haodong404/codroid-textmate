package org.codroid.textmate.grammar

import org.codroid.textmate.theme.ScopeName


interface AbsoluteRuleReference {
    val scopeName: ScopeName
    fun toKey(): String
}

/**
 * References the top level rule of a grammar with the given scope name.
 */
class TopLevelRuleReference(override val scopeName: ScopeName) : AbsoluteRuleReference {
    override fun toKey(): String = scopeName
}

/**
 * References a rule of a grammar in the top level repository section with the given name.
 */
class TopLevelRepositoryRuleReference(override val scopeName: ScopeName, val ruleName: String) :
    AbsoluteRuleReference {
    override fun toKey(): String = "$scopeName#$ruleName"
}

class ExternalReferenceCollector {
    private val references = mutableListOf<AbsoluteRuleReference>()
    private val seenReferenceKeys = mutableSetOf<String>()
    val visitedRule = mutableSetOf<RawRule>()

    fun getReferences(): List<AbsoluteRuleReference> = references

    fun add(reference: AbsoluteRuleReference) {
        reference.toKey().let {
            if (seenReferenceKeys.contains(it)) {
                return
            }
            seenReferenceKeys.add(it)
            references.add(reference)
        }
    }
}

class ScopeDependencyProcessor(
    private val repo: GrammarRepository,
    private val initialScopeName: ScopeName
) {
    private val seenFullScopeRequests = mutableSetOf<ScopeName>()
    private val seenPartialScopeRequests = mutableListOf<ScopeName>()
    var qq: MutableList<AbsoluteRuleReference>

    init {
        seenFullScopeRequests.add(initialScopeName)
        qq = mutableListOf(TopLevelRuleReference(initialScopeName))
    }

    fun processQueue() {
        val q = qq
        qq = mutableListOf()

        val deps = ExternalReferenceCollector()
        for (dep in q) {
            collectReferencesOfReference(dep, initialScopeName, repo, deps)
        }
        for (dep in deps.getReferences()) {
            if (dep is TopLevelRuleReference) {
                if (seenFullScopeRequests.contains(dep.scopeName)) {
                    // already processed
                    continue
                }
                seenFullScopeRequests.add(dep.scopeName)
                qq.add(dep)
            } else {
                if (seenFullScopeRequests.contains(dep.scopeName)) continue
                if (seenPartialScopeRequests.contains(dep.toKey())) continue
                seenPartialScopeRequests.add(dep.toKey())
                qq.add(dep)
            }
        }
    }
}

fun collectReferencesOfReference(
    reference: AbsoluteRuleReference,
    baseGrammarScopeName: ScopeName,
    repo: GrammarRepository,
    result: ExternalReferenceCollector
) {
    val selfGrammar = repo.lookup(reference.scopeName)
    if (selfGrammar == null) {
        if (reference.scopeName == baseGrammarScopeName) {
            throw NullPointerException("No grammar provided for <$baseGrammarScopeName>")
        }
        return
    }
    val baseGrammar = repo.lookup(baseGrammarScopeName)
    if (baseGrammar != null) {
        if (reference is TopLevelRuleReference) {
            collectExternalReferencesInTopLevelRule(Context(baseGrammar, selfGrammar), result)
        } else {
            collectExternalReferencesInTopLevelRepositoryRule(
                (reference as TopLevelRepositoryRuleReference).ruleName,
                ContextWithRepository(baseGrammar, selfGrammar, selfGrammar.repository),
                result
            )
        }
        val injections = repo.injections(reference.scopeName) ?: emptyArray()
        for (injection in injections) {
            result.add(TopLevelRuleReference(injection))
        }
    }

}

open class Context(open val baseGrammar: RawGrammar, open val selfGrammar: RawGrammar)

class ContextWithRepository(
    override var baseGrammar: RawGrammar,
    override val selfGrammar: RawGrammar,
    val repository: RawRepository? = null
) : Context(baseGrammar, selfGrammar) {
    constructor(context: Context, repository: RawRepository? = null) : this(
        context.baseGrammar,
        context.selfGrammar,
        repository
    )
}

fun collectExternalReferencesInTopLevelRepositoryRule(
    ruleName: String,
    context: ContextWithRepository,
    result: ExternalReferenceCollector
) {
    context.repository?.let {
        val temp = it[ruleName]
        if (temp != null) {
            collectExternalReferencesInRules(arrayOf(temp), context, result)
        }
    }
}

fun collectExternalReferencesInTopLevelRule(context: Context, result: ExternalReferenceCollector) {
    if (context.selfGrammar.patterns.isNotEmpty()) {
        collectExternalReferencesInRules(
            context.selfGrammar.patterns,
            ContextWithRepository(
                context,
                repository = context.selfGrammar.repository
            ), result
        )
    }

    if (!context.selfGrammar.injections.isNullOrEmpty()) {
        collectExternalReferencesInRules(
            context.selfGrammar.injections!!.values.toTypedArray(),
            ContextWithRepository(
                context = context,
                repository = context.selfGrammar.repository
            ), result
        )
    }
}

fun collectExternalReferencesInRules(
    rules: Array<RawRule>,
    context: ContextWithRepository,
    result: ExternalReferenceCollector
) {
    for (rule in rules) {
        if (result.visitedRule.contains(rule)) continue
        result.visitedRule.add(rule)

        val patternRepository = if (rule.repository != null) {
            val temp = rule.repository!!.plus(context.repository ?: emptyMap())
            RawRepository(temp)
        } else {
            context.repository
        }
        if (rule.patterns != null) {
            collectExternalReferencesInRules(
                rule.patterns!!,
                ContextWithRepository(
                    context = context,
                    repository = patternRepository
                ), result
            )
        }
        val include = rule.include ?: continue
        val reference = parseInclude(include)
        when (reference.kind) {
            IncludeReferenceKind.Base ->
                collectExternalReferencesInTopLevelRule(
                    Context(
                        /*
                        *  Waiting for validation...
                        */
                        selfGrammar = context.baseGrammar,
                        baseGrammar = context.baseGrammar
                    ), result
                )

            IncludeReferenceKind.Self ->
                collectExternalReferencesInTopLevelRule(context, result)

            IncludeReferenceKind.RelativeReference ->
                collectExternalReferencesInTopLevelRepositoryRule(
                    (reference as RelativeReference).ruleName,
                    ContextWithRepository(context, patternRepository),
                    result
                )

            IncludeReferenceKind.TopLevelReference,
            IncludeReferenceKind.TopLevelRepositoryReference -> {
                val selfGrammar =
                    if ((reference as TopLevel).scopeName.contentEquals(context.selfGrammar.scopeName)) {
                        context.selfGrammar
                    } else if (reference.scopeName.contentEquals(context.baseGrammar.scopeName)) {
                        context.baseGrammar
                    } else {
                        null
                    }
                if (selfGrammar != null) {
                    val newContext = ContextWithRepository(
                        baseGrammar = context.baseGrammar,
                        selfGrammar = selfGrammar,
                        repository = patternRepository
                    )
                    if (reference is TopLevelRepositoryReference) {
                        collectExternalReferencesInTopLevelRepositoryRule(reference.ruleName, newContext, result)
                    } else {
                        collectExternalReferencesInTopLevelRule(newContext, result)
                    }
                } else {
                    if (reference is TopLevelRepositoryReference) {
                        result.add(TopLevelRepositoryRuleReference(reference.scopeName, reference.ruleName))
                    } else {
                        result.add(TopLevelRuleReference(reference.scopeName))
                    }
                }
            }
        }
    }
}

fun parseInclude(include: String): IncludeReference {
    if (include.contentEquals("\$base")) {
        return BaseReference()
    } else if (include.contentEquals("\$self")) {
        return SelfReference()
    }

    include.indexOf('#').let {
        return when (it) {
            -1 -> TopLevelReference(include)
            0 -> RelativeReference(include.substring(1))
            else -> TopLevelRepositoryReference(
                include.substring(0, it),
                include.substring(it + 1)
            )
        }
    }
}