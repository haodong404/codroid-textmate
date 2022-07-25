package org.codroid.textmate.theme

data class StyleAttributes(
    val fontStyle: FontStyle, val foregroundId: UInt, val backgroundId: UInt
) {
    override fun equals(other: Any?): Boolean {
        other?.let {
            if (other !is StyleAttributes) {
                return false
            }
            return fontStyle == other.fontStyle && foregroundId == other.foregroundId && backgroundId == other.backgroundId
        }
        return false
    }

    override fun hashCode(): Int {
        var result = fontStyle.toInt()
        result = 31 * result + foregroundId.hashCode()
        result = 31 * result + backgroundId.hashCode()
        return result
    }
}