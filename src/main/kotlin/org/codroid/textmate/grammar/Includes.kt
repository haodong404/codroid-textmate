package org.codroid.textmate.grammar

import org.codroid.textmate.theme.ScopeName


interface IncludeReference {
    val kind: IncludeReferenceKind
}

enum class IncludeReferenceKind {
    Base, Self, RelativeReference, TopLevelReference, TopLevelRepositoryReference
}

class BaseReference : IncludeReference {
    override val kind: IncludeReferenceKind
        get() = IncludeReferenceKind.Base
}

class SelfReference : IncludeReference {
    override val kind: IncludeReferenceKind
        get() = IncludeReferenceKind.Self
}

class RelativeReference(val scopeName: ScopeName) : IncludeReference {
    override val kind: IncludeReferenceKind
        get() = IncludeReferenceKind.RelativeReference
}

class TopLevelReference(val scopeName: ScopeName) : IncludeReference {
    override val kind: IncludeReferenceKind
        get() = IncludeReferenceKind.TopLevelReference
}

class TopLevelRepositoryReference(
    val scopeName: ScopeName, val ruleName: String
) : IncludeReference {
    override val kind: IncludeReferenceKind
        get() = IncludeReferenceKind.TopLevelRepositoryReference

}