package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals

class CharBufferTest {

    @Test
    fun substringTest() {
        val txt = "HelloWorld"
        assertEquals(txt.substring(1, 9), txt.toCharArray().toCharBuffer().subString(1, 9))
    }
}