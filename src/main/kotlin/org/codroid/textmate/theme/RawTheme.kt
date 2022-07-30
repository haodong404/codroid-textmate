package org.codroid.textmate.theme

data class RawTheme(var name: String? = null, var settings: Array<RawThemeSetting>? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawTheme

        if (name != other.name) return false
        if (!settings.contentEquals(other.settings)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + settings.contentHashCode()
        return result
    }
}
