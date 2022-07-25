package org.codroid.textmate.theme

class ColorMap(colorMap: Array<String>? = null) {
    private val isFrozen: Boolean
    private var lastColorId = 0u
    private val id2color = mutableMapOf<UInt, String>()
    private val color2id = mutableMapOf<String, UInt>()

    init {
        if (colorMap != null) {
            isFrozen = true
            for ((idx, item) in colorMap.withIndex()) {
                color2id[item] = idx.toUInt()
                id2color[idx.toUInt()] = item
            }
        } else {
            isFrozen = false
        }
    }

    fun getId(color: String?): UInt {
        if (color == null) return 0u
        val colorUpper = color.uppercase()
        var value = color2id[colorUpper]
        if (value != null) {
            return value
        }
        if (this.isFrozen) {
            throw IllegalArgumentException("Missing color in color map - $color")
        }
        value = ++this.lastColorId
        color2id[color] = value
        id2color[value] = color
        return value
    }

    fun getColorMap(): Map<UInt, String> = id2color
    override fun toString(): String {
        return "ColorMap(isFrozen=$isFrozen, lastColorId=$lastColorId, id2color=$id2color, color2id=$color2id)"
    }

    override fun equals(other: Any?): Boolean {
        other?.let {
            if (other !is ColorMap) {
                return false
            }
            return toString().contentEquals(other.toString())
        }
        return false
    }
}