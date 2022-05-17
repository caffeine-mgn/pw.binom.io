package pw.binom.io.file

import kotlin.test.Test
import kotlin.test.assertEquals

class PosixPermissionsTest {
    @Test
    fun testParse() {
        fun test(txt: String) {
            assertEquals(txt, PosixPermissions.parse(txt)?.toString())
        }
        test("d------r--")
        test("drwx--x--x")
        test("drwxr-xr-x")
        test("---S--S--T")
        test("---s--s--t")
    }
}
