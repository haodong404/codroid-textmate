package org.codroid.textmate.utils

interface OnigLib {
    fun createOnigScanner(source: Array<String>): OnigScanner
    fun createOnigString(str: String): OnigString
}

data class OnigCaptureIndex(val start: Int, val end: Int, val length: Int)

data class OnigMatch(val index: Int, val captureIndices: Array<OnigCaptureIndex>)

typealias FindOption = Byte

object FindOptionConsts {
    const val None: FindOption = 0

    /**
     * equivalent of ONIG_OPTION_NOT_BEGIN_STRING: (str) isn't considered as begin of string (* fail \A)
     */
    const val NotBeginString: FindOption = 1

    /**
     * equivalent of ONIG_OPTION_NOT_END_STRING: (end) isn't considered as end of string (* fail \z, \Z)
     */
    const val NotEndString: FindOption = 2

    /**
     * equivalent of ONIG_OPTION_NOT_BEGIN_POSITION: (start) isn't considered as start position of search (* fail \G)
     */
    const val NotBeginPosition: FindOption = 4

    /**
     * used for debugging purposes.
     */
    const val DebugCall: FindOption = 8
}

data class OnigScanner(
    val findNextMatchSync: (string: OnigString, startPosition: Int, options: FindOption) -> OnigMatch?,
    val dispose: () -> Unit?
)

data class OnigString(
    val content: String,
    val dispose: () -> Unit?
)

fun disposeOnigString(str: OnigString) {
    str.dispose()
}