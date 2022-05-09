package pw.binom

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringUtilTest {
    @Test
    fun testWildcardMatch() {
        fun check(num: Int) {
            val sb = StringBuilder()
            sb.append("123")
            repeat(num) {
                sb.append("O")
            }
            sb.append("45")
            assertTrue(sb.toString().isWildcardMatch("*123*45"))
        }
        check(9999)

        assertTrue("/test/hello".isWildcardMatch("/*"))
        assertTrue("/test/hello".isWildcardMatch("/*o"))
        assertTrue("/test/hello".isWildcardMatch("/*?lo"))
        assertFalse("test/hello".isWildcardMatch("/*"))
        assertFalse("test/hello".isWildcardMatch("/**"))
        assertTrue("t123o".isWildcardMatch("t***o"))
        assertTrue("antoon".isWildcardMatch("ant?on"))
        assertTrue("antoon".isWildcardMatch("ant?o?"))
        assertTrue("antoon".isWildcardMatch("?nt*n"))
        assertTrue("antoon".isWildcardMatch("???*"))
        assertFalse("an".isWildcardMatch("???*"))
    }
}
