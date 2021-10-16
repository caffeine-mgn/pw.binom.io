package pw.binom.net

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PathTest {

    @Test
    fun parentTest() {
        assertEquals("", assertNotNull("/ololo".toPath.parent).toString())
        assertEquals("/binom", assertNotNull("/binom/ololo".toPath.parent).toString())
        assertNull("".toPath.parent)
    }

    @Test
    fun nameTest(){
        assertEquals("io","/pw/binom/io".toPath.name)
    }
}