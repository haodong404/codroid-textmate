package org.codroid.textmate.grammar.tokenize

import org.codroid.textmate.grammar.AttributedScopeStack

data class LocalStackElement(val scopes: AttributedScopeStack, val endPos: Int)