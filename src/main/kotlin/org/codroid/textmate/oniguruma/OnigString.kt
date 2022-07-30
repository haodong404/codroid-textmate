package org.codroid.textmate.oniguruma

import org.jcodings.specific.UTF8Encoding
import java.nio.charset.StandardCharsets
import java.util.*

abstract class OnigString(
    open val content: String, open val bytesUTF8: ByteArray
) {
    val bytesCount by lazy {
        bytesUTF8.size
    }

    companion object {
        fun create(string: String): OnigString {
            string.toByteArray(StandardCharsets.UTF_8).let {
                if (it.size == string.length) {
                    return SingleByteString(string, it)
                }
                return MultiByteString(string, it)
            }
        }
    }

    abstract fun getByteIndexOfChar(charIndex: Int): Int

    abstract fun getCharIndexOfByte(byteIndex: Int): Int

    abstract fun dispose()
}

class MultiByteString(
    override val content: String, override val bytesUTF8: ByteArray
) : OnigString(content, bytesUTF8) {

    private val lastCharIndex = content.length - 1
    private var byteToCharOffsets: IntArray? = null

    override fun getByteIndexOfChar(charIndex: Int): Int {
        if (charIndex == lastCharIndex + 1) {
            return lastCharIndex
        }

        if (charIndex < 0 || charIndex > lastCharIndex) {
            throw IndexOutOfBoundsException("$charIndex, $lastCharIndex")
        }
        if (charIndex == 0) {
            return 0
        }

        val byteToCharOffsets = getByteToCharOffsets()
        var byteIndex = Arrays.binarySearch(byteToCharOffsets, charIndex)
        while (byteIndex > 0 && byteToCharOffsets[byteIndex - 1] == charIndex) {
            byteIndex--
        }
        return byteIndex
    }

    private fun getByteToCharOffsets(): IntArray {
        var offsets: IntArray? = byteToCharOffsets
        if (offsets?.isEmpty() == true) {
            offsets = IntArray(bytesCount)
            var charIndex = 0
            var byteIndex = 0
            val maxByteIndex: Int = bytesCount - 1
            while (byteIndex <= maxByteIndex) {
                val charLenInBytes = UTF8Encoding.INSTANCE.length(bytesUTF8, byteIndex, bytesCount)
                // same as "Arrays.fill(offsets, byteIndex, byteIndex + charLenInBytes, charIndex)" but faster
                val l = byteIndex + charLenInBytes
                while (byteIndex < l) {
                    offsets[byteIndex] = charIndex
                    byteIndex++
                }
                charIndex++
            }
            byteToCharOffsets = offsets
        }
        return offsets ?: IntArray(0)
    }

    override fun getCharIndexOfByte(byteIndex: Int): Int {
        if (byteIndex == bytesCount) {
            // One off can happen when finding the end of a regexp (it's the right boundary).
            return lastCharIndex + 1
        }

        if (byteIndex < 0 || byteIndex >= bytesCount) {
            throw IndexOutOfBoundsException("$byteIndex, $bytesCount")
        }
        return if (byteIndex == 0) {
            0
        } else getByteToCharOffsets()[byteIndex]

    }

    override fun dispose() {
        TODO()
    }

}

class SingleByteString(
    override val content: String, override val bytesUTF8: ByteArray
) : OnigString(content, bytesUTF8) {

    override fun getByteIndexOfChar(charIndex: Int): Int {
        if (charIndex == bytesCount) {
            // One off can happen when finding the end of a regexp (it's the right boundary).
            return charIndex
        }

        if (charIndex < 0 || charIndex >= bytesCount) {
            throw IndexOutOfBoundsException("$charIndex, $bytesCount")
        }
        return charIndex
    }

    override fun getCharIndexOfByte(byteIndex: Int): Int {
        if (byteIndex == bytesCount) {
            // One off can happen when finding the end of a regexp (it's the right boundary).
            return byteIndex
        }

        if (byteIndex < 0 || byteIndex >= bytesCount) {
            throw IndexOutOfBoundsException("$byteIndex, $bytesCount")
        }
        return byteIndex
    }

    override fun dispose() {

    }

}