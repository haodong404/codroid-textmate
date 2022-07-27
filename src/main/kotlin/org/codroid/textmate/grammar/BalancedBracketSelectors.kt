package org.codroid.textmate.grammar

import org.codroid.textmate.Matchers

class BalancedBracketSelectors(
    balancedBracketScopes: Array<String>,
    unbalancedBracketScopes: Array<String>,
) {
    private var allowAny = false
    private val balancedBracketScopes = balancedBracketScopes.flatMap {
        if (it == "*") {
            allowAny = true
            return@flatMap emptyList()
        }
        return@flatMap Matchers.create(it, ::nameMatcher)
    }
    private val unbalancedBracketScopes = balancedBracketScopes.flatMap {
        Matchers.create(it, ::nameMatcher).map { m -> m.matcher }
    }

    fun getMatchersAlways(): Boolean =
        this.allowAny && this.unbalancedBracketScopes.isEmpty()

    fun getMatchesNever(): Boolean =
        this.balancedBracketScopes.isEmpty() && !this.allowAny

    fun match(scopes: Array<String>): Boolean {
        unbalancedBracketScopes.forEach { if (it(scopes)) return false }
        balancedBracketScopes.forEach { if (it.matcher(scopes)) return true }
        return this.allowAny
    }
}