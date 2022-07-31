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

class RelativeReference(val ruleName: ScopeName) : IncludeReference {
    override val kind: IncludeReferenceKind
        get() = IncludeReferenceKind.RelativeReference
}

interface TopLevel {
    val scopeName: ScopeName
}

class TopLevelReference(override val scopeName: ScopeName) : IncludeReference, TopLevel {
    override val kind: IncludeReferenceKind
        get() = IncludeReferenceKind.TopLevelReference
}

class TopLevelRepositoryReference(
    override val scopeName: ScopeName, val ruleName: String
) : IncludeReference, TopLevel {
    override val kind: IncludeReferenceKind
        get() = IncludeReferenceKind.TopLevelRepositoryReference

}