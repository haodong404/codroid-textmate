package oniguruma

import org.joni.Region

class OnigResult(val region: Region, var indexInScanner: Int) {

    fun locationAt(index: Int): Int {
        val bytes = region.beg[index]
        if (bytes > 0) {
            return bytes
        }
        return 0
    }

    fun count(): Int {
        return region.numRegs
    }

    fun lengthAt(index: Int): Int {
        val bytes = region.end[index] - region.beg[index]
        if (bytes > 0) {
            return bytes
        }
        return 0
    }
}