package pw.binom

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringUtilTest {
    @Test
    fun testWildcardMatch() {
        assertTrue("/test/hello".isWildcardMatch("/*"))
        assertTrue("/test/hello".isWildcardMatch("/*o"))
        assertTrue("/test/hello".isWildcardMatch("/*?lo"))
        assertFalse("test/hello".isWildcardMatch("/*"))
    }
}