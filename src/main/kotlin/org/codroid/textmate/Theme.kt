package org.codroid.textmate

class Theme {

}

typealias FontStyle = Byte

object FontStyleConsts {
    const val NotSet: FontStyle = -1
    const val None: FontStyle = 0
    const val Italic: FontStyle = 1
    const val Bold: FontStyle = 2
    const val Underline: FontStyle = 4
    const val Strikethrough: FontStyle = 8
}