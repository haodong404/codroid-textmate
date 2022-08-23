package org.codroid.textmate.theme

data class StyleAttributes(
    val fontStyle: FontStyle, val foregroundId: Int, val backgroundId: Int,
    val caretId: Int = 0, val lineHighlightId: Int = 0, val selectionId: Int = 0,
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