package org.codroid.textmate.grammar

import org.codroid.textmate.ScopeName

interface GrammarRepository {
    fun lookup(scopeName: ScopeName): RawGrammar?
    fun injections(scopeName: ScopeName): Array<ScopeName>
}