package org.codroid.textmate.theme

class ColorMap(colorMap: Array<String>? = null) {
    private val isFrozen: Boolean
    private var lastColorId = 0
    private val id2color = mutableMapOf<Int, String>()
    private val color2id = mutableMapOf<String, Int>()

    init {
        if (colorMap != null) {
            isFrozen = true
            for ((idx, item) in colorMap.withIndex()) {
                color2id[item] = idx
                id2color[idx] = item
            }
        } else {
            isFrozen = false
        }
    }

    fun getId(color: String?): Int {
        if (color == null) return 0
        val colorUpper = color.uppercase()
        var value = color2id[colorUpper]
        if (value != null) {
            return value
        }
        if (this.isFrozen) {
            throw IllegalArgumentException("Missing color in color map - $colorUpper")
        }
        value = ++this.lastColorId
        color2id[colorUpper] = value
        id2color[value] = colorUpper
        return value
    }

    fun getColorMap(): Map<Int, String> = id2color
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