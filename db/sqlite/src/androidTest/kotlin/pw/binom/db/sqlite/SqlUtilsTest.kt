package pw.binom.db.sqlite

import kotlin.test.Test
import kotlin.test.assertEquals

class SqlUtilsTest {
    @Test
    fun splitQueryStatementsTest() {
        val p = SqlUtils.splitQueryStatements("""select [cc;c]; select ';1;23'""")
        assertEquals(2, p.size)
        assertEquals("select [cc;c]", p[0])
        assertEquals(" select ';1;23'", p[1])
    }
}
