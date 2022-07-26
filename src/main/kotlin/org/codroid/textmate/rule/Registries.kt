package org.codroid.textmate.rule

import org.codroid.textmate.grammar.RawGrammar
import org.codroid.textmate.grammar.RawRepository
import org.codroid.textmate.theme.ScopeName
import org.codroid.textmate.utils.OnigLib

interface RuleRegistry {
    fun getRule(ruleId: RuleId): Rule
    fun <T : Rule> registerRule(factor: (id: RuleId) -> T): T
}

interface GrammarRegistry {
    fun getExternalGrammar(scopeName: ScopeName, repository: RawRepository): RawGrammar?
}

interface RuleRegistryOnigLib : RuleRegistry, OnigLib {

}