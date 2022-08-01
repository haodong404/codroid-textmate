import org.codroid.textmate.every
import org.codroid.textmate.some
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UtilsTest {
    @Test
    fun `Test 'some' and 'every' function for a List`() {
        val sample = listOf(true, false, true)
        assertTrue(sample.some { it })
        assertFalse { sample.every { it } }

        val sample2 = listOf(true, true, true)
        assertTrue(sample2.every { it })
        assertTrue { sample2.some { it } }

        val sample3 = listOf(false, false, false)
        assertFalse { sample3.every { it } }
        assertFalse { sample3.some { it } }
    }

    @Test
    fun `Test 'some' and 'every' when the list is empty`() {
        val sample = listOf<Boolean>()
        assertTrue(sample.every { it })
        assertFalse { sample.some { it } }
    }
}