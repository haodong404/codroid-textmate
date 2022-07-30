package org.codroid.textmate.grammar

import org.codroid.textmate.StackElementDef
import org.codroid.textmate.rule.Rule
import org.codroid.textmate.rule.RuleId
import org.codroid.textmate.rule.RuleRegistry

/**
 * Represents a "pushed" state on the stack (as a linked list element).
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/caab3de34a8cc7182141c9e31e0f42b96a3a1bac/src/grammar/grammar.ts#L550">
 *     src/grammar/grammar.ts#L550<a/>
 */
class StateStack(
    /**
     * The previous state on the stack (or null for the root state).
     */
    val parent: StateStack?,
    /**
     * The state (rule) that this element represents.
     */
    val ruleId: RuleId,

    var enterPos: Int,
    var anchorPos: Int,

    /**
     * The state has entered and captured \n. This means that the next line should have an anchorPosition of 0.
     */
    val beginRuleCapturedEOL: Boolean,
    /**
     * The state has entered and captured \n. This means that the next line should have an anchorPosition of 0.
     */
    val endRule: String?,
    /**
     * The list of scopes containing the "name" for this state.
     */
    val nameScopesList: AttributedScopeStack,
    /**
     * The list of scopes containing the "contentName" (besides "name") for this state.
     * This list **must** contain as an element `scopeName`.
     */
    val contentNameScopesList: AttributedScopeStack

) : StackElementDef {

    override var depth: Int = if (this.parent != null) {
        this.parent.depth
    } else {
        1
    }
    override var stackElementBrand: Unit = Unit

    companion object {
        val Null = StateStack(
            null,
            RuleId.from(0),
            0, 0, false, null, AttributedScopeStack.createRoot("", 0u), AttributedScopeStack.createRoot("", 0u)
        )
    }

    private fun reset(new: StateStack?) {
        var newClone = new?.clone()
        while (newClone != null) {
            newClone.enterPos = -1
            newClone.anchorPos = -1
            newClone = newClone.parent
        }
    }

    fun reset() {
        reset(this)
    }

    override fun clone(): StateStack = StateStack(
        parent,
        ruleId,
        enterPos,
        anchorPos,
        beginRuleCapturedEOL,
        endRule,
        nameScopesList,
        contentNameScopesList
    )

    private fun equals(first: StateStack, second: StateStack): Boolean {
        if (first === second) {
            return true
        }
        if (!this.structuralEquals(first, second)) {
            return false
        }
        return first.contentNameScopesList == second.contentNameScopesList
    }

    private fun structuralEquals(
        first: StateStack?, second: StateStack?
    ): Boolean {
        var a = first
        var b = second
        do {
            if (a === b) {
                return true
            }
            if (a == null && b == null) {
                // End of list reached for both
                return true
            }
            if (a == null || b == null) {
                return false
            }
            if (a.depth != b.depth || a.ruleId != b.ruleId || a.endRule != b.endRule) {
                return false
            }
            // Go to previous pair
            a = a.parent
            b = b.parent
        } while (true)
    }

    override fun equals(other: Any?): Boolean {
        if (other is StateStack) {
            return equals(this, other)
        }
        return false
    }

    fun pop(): StateStack? = this.parent

    fun satePop(): StateStack = this.parent ?: this

    fun push(
        ruleId: RuleId,
        enterPos: Int,
        anchorPos: Int,
        beginRuleCapturedEOL: Boolean,
        endRule: String?,
        nameScopesList: AttributedScopeStack,
        contentNameScopesList: AttributedScopeStack
    ): StateStack = StateStack(
        this,
        ruleId,
        enterPos,
        anchorPos,
        beginRuleCapturedEOL,
        endRule,
        nameScopesList,
        contentNameScopesList
    )

    fun getRule(grammar: RuleRegistry): Rule = grammar.getRule(this.ruleId)

    private fun writeString(res: Array<String>, outIndex: Int): Int {
        var outIndexClone = outIndex
        this.parent?.let { outIndexClone = this.parent.writeString(res, outIndexClone) }
        res[outIndexClone++] = "(${this.ruleId}, TODO-${this.nameScopesList}, TODO-${this.contentNameScopesList}"
        return outIndexClone
    }

    override fun toString(): String {
        val result = mutableListOf<String>()
        this.writeString(result.toTypedArray(), 0)
        return "[${result.joinToString(",")}]"
    }

    fun withContentNameScopesList(contentNameScopeStack: AttributedScopeStack): StateStack {
        if (this.contentNameScopesList == contentNameScopesList) {
            return this
        }
        return this.parent!!.push(
            this.ruleId, this.enterPos, this.anchorPos, this.beginRuleCapturedEOL,
            this.endRule, this.nameScopesList, contentNameScopesList
        )
    }

    fun withEndRule(endRule: String): StateStack {
        if (this.endRule == endRule) {
            return this
        }
        return StateStack(
            this.parent, this.ruleId, this.enterPos, this.anchorPos, this.beginRuleCapturedEOL,
            endRule, this.nameScopesList, this.contentNameScopesList
        )
    }

    // Used to warn of endless loops
    fun hasSameRuleAs(other: StateStack): Boolean {
        var el: StateStack? = this
        while (el != null && el.enterPos == other.enterPos) {
            if (el.ruleId == other.ruleId) {
                return true
            }
            el = el.parent
        }
        return false
    }

    override fun hashCode(): Int {
        var result = stackElementBrand.hashCode()
        result = 31 * result + depth
        return result
    }

}