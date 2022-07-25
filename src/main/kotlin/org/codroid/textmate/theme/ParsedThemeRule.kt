package org.codroid.textmate.theme

data class ParsedThemeRule(
    val scope: ScopeName,
    val parentScopes: List<ScopeName>?,
    val index: Int,
    val fontStyle: FontStyle,
    val foreground: String?,
    val background: String?
)