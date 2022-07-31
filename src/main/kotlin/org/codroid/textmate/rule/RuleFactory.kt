package org.codroid.textmate.rule

import org.codroid.textmate.grammar.*

interface RuleFactoryHelper : RuleRegistry, GrammarRegistry

object RuleFactory {
    fun createCaptureRule(
        helper: RuleFactoryHelper,
        location: Location? = null,
        name: String? = null,
        contentName: String? = null,
        retokenizeCaptureWithRuleId: RuleId = RuleId.from(0)
    ): CaptureRule = helper.registerRule {
        return@registerRule CaptureRule(location, it, name, contentName, retokenizeCaptureWithRuleId)
    }

    fun getCompiledRuleId(desc: RawRule, helper: RuleFactoryHelper, repository: RawRepository): RuleId {
        if (desc.id == null) {
            helper.registerRule {
                desc.id = it
                if (desc.match != null) {
                    return@registerRule MatchRule(
                        desc.location, desc.id!!, desc.name, desc.match,
                        compileCaptures(desc.captures, helper, repository)
                    )
                }
                if (desc.begin == null) {
                    if (desc.repository != null) {
                        repository.map = RawRepositoryMap(desc.repository.map?.plus(repository.map!!))
                        if (desc.repository.location != null) repository.location = desc.repository.location
                    }
                    var patterns = desc.patterns
                    if (patterns == null && desc.include != null) {
                        patterns = arrayOf(RawRule(include = desc.include))
                    }
                    return@registerRule IncludeOnlyRule(
                        desc.location, desc.id!!, desc.name, desc.contentName,
                        compilePatterns(patterns, helper, repository)
                    )
                }

                if (desc.while_ != null) {
                    return@registerRule BeginWhileRule(
                        desc.location, desc.id!!, desc.name, desc.contentName,
                        desc.begin, compileCaptures(desc.beginCaptures ?: desc.captures, helper, repository),
                        desc.while_, compileCaptures(desc.whileCaptures ?: desc.captures, helper, repository),
                        compilePatterns(desc.patterns, helper, repository)
                    )
                }
                return@registerRule BeginEndRule(
                    desc.location, desc.id!!, desc.name, desc.contentName,
                    desc.begin, compileCaptures(desc.beginCaptures ?: desc.captures, helper, repository),
                    desc.end, compileCaptures(desc.endCaptures ?: desc.captures, helper, repository),
                    desc.applyEndPatternLast ?: false, compilePatterns(desc.patterns, helper, repository)
                )
            }
        }
        return desc.id!!
    }

    private fun compileCaptures(
        captures: RawCaptures?,
        helper: RuleFactoryHelper,
        repository: RawRepository
    ): Array<CaptureRule?> {
        val result = mutableListOf<CaptureRule?>()
        if (captures != null) {
            //Find the maximum capture id
            var maximumCaptureId = 0
            for (captureId in captures.map?.keys ?: emptyList()) {
                if (captureId.contentEquals("location")) {
                    continue
                }
                Integer.parseInt(captureId, 10).let {
                    if (it > maximumCaptureId) {
                        maximumCaptureId = it
                    }
                }
            }

            // Initialize result
            for (i in 0..maximumCaptureId) {
                result.add(null)
            }

            // Fill out result
            for (captureId in captures.map?.keys ?: emptyList()) {
                if (captureId.contentEquals("location")) {
                    continue
                }
                Integer.parseInt(captureId, 10).let {
                    var retokenizeCapturedWithRuleId = RuleId.from(0)
                    if (captures.map!![captureId]?.patterns != null) {
                        retokenizeCapturedWithRuleId = getCompiledRuleId(captures.map[captureId]!!, helper, repository)
                    }
                    captures.map[captureId]?.let { c ->
                        result[it] =
                            createCaptureRule(helper, c.location, c.name, c.contentName, retokenizeCapturedWithRuleId)
                    }
                }
            }
        }
        return result.toTypedArray()
    }

    private fun compilePatterns(
        patterns: Array<RawRule>?,
        helper: RuleFactoryHelper,
        repository: RawRepository
    ): CompilePatternsResult {
        val result = mutableListOf<RuleId>()

        if (patterns != null) {
            for (pattern in patterns) {
                var ruleId = RuleId.End
                if (pattern.include != null) {
                    val reference = parseInclude(pattern.include)
                    when (reference) {
                        is BaseReference -> {}
                        is SelfReference -> ruleId = getCompiledRuleId(
                            repository.map!!.getOrDefault(pattern.include, RawRule()),
                            helper,
                            repository
                        )

                        is RelativeReference -> {
                            // Local include found in `repository`
                            repository.map?.get(reference.ruleName)?.let {
                                ruleId = getCompiledRuleId(it, helper, repository)
                            }
                        }

                        is TopLevelReference,
                        is TopLevelRepositoryReference -> {
                            val externalGrammarName = (reference as TopLevel).scopeName
                            val externalGrammarInclude = if (reference is TopLevelRepositoryReference) {
                                reference.ruleName
                            } else {
                                null
                            }
                            // External include
                            helper.getExternalGrammar(externalGrammarName, repository)?.let { externalGrammar ->
                                if (externalGrammarInclude != null) {
                                    externalGrammar.repository.map?.get(externalGrammarInclude)?.let {
                                        ruleId = getCompiledRuleId(it, helper, repository)
                                    }
                                } else {
                                    ruleId = getCompiledRuleId(
                                        externalGrammar.repository.map!!["\$self"]!!,
                                        helper,
                                        externalGrammar.repository
                                    )
                                }
                            }

                        }
                    }
                } else {
                    ruleId = getCompiledRuleId(pattern, helper, repository)
                }

                if (ruleId != RuleId.End) {
                    val rule = helper.getRule(ruleId)
                    var skipRule = false
                    if (rule is WithPatternRule) {
                        if (rule.hasMissingPatterns && rule.patterns.isEmpty()) {
                            skipRule = true
                        }
                    }
                    if (skipRule) {
                        continue
                    }
                    result.add(ruleId)
                }
            }
        }
        return CompilePatternsResult(
            result.toTypedArray(),
            (patterns?.size ?: 0) != result.size
        )
    }
}