package org.codroid.textmate.theme

data class Setting(
    var fontStyle: String? = null, var foreground: String? = null, var background: String? = null
) {
    override fun equals(other: Any?): Boolean {
        other?.let {
            if (other !is Setting) {
                return false
            }
            return fontStyle == other.fontStyle && foreground == other.foreground && background == other.background
        }
        return false
    }

    override fun hashCode(): Int {
        var result = fontStyle?.hashCode() ?: 0
        result = 31 * result + (foreground?.hashCode() ?: 0)
        result = 31 * result + (background?.hashCode() ?: 0)
        return result
    }
}

data class RawThemeSetting(
    var name: String? = null,
    var scopes: Array<ScopePattern>? = null,
    var scope: ScopePattern? = null,
    var settings: Setting? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawThemeSetting

        if (name != other.name) return false
        if (!scope.contentEquals(other.scope)) return false
        if (settings != other.settings) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + scope.hashCode()
        result = 31 * result + settings.hashCode()
        return result
    }
}