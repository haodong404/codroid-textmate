package org.codroid.textmate.oniguruma

class OnigSearcher(regExps: Array<String>) {
    private val regExps = regExps.map { OnigRegExp(it) }

    fun search(source: OnigString, charOffset: Int): OnigResult? {
        val byteOffset = source.getByteIndexOfChar(charOffset)
        var bestLocation = 0
        var bestResult: OnigResult? = null
        for ((index, regExp) in this.regExps.withIndex()) {
            val result = regExp.search(source, byteOffset)
            if (result != null && result.count() > 0) {
                val location = result.locationAt(0)
                if (bestResult == null || location < bestLocation) {
                    bestLocation = location
                    bestResult = result
                    bestResult.indexInScanner = index
                }
                if (location == byteOffset) {
                    break
                }
            }
        }
        return bestResult
    }
}