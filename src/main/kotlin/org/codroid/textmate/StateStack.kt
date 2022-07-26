package org.codroid.textmate

/**
 * **IMPORTANT** - Immutable!
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/caab3de34a8cc7182141c9e31e0f42b96a3a1bac/src/main.ts#L255">
 *     src/main.ts#L255<a/>
 */
interface StateStack : Cloneable {
    var stackElementBrand: Unit
    val depth: Int

    override fun clone(): StateStack
    override fun equals(other: Any?): Boolean
}