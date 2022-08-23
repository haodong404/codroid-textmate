package org.codroid.textmate.theme

data class ParsedThemeRule(
    val scope: ScopeName,
    val parentScopes: List<ScopeName>?,
    val index: Int,
    val fontStyle: FontStyle,
    val foreground: String?,
    val background: String?,

    val caret: String? = null, val lineHighlight: String? = null, val selection: String? = null,
)