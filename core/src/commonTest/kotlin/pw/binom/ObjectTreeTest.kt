package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals

private class MyClass(var data: Int)

class ObjectTreeTest {

    @Test
    fun test() {
        val d = ObjectTree {
            ObjectTree { MyClass(20) }
                .attach().let {
                    it.data++
                    it
                }
        }.attach()
        assertEquals(21, d.data)
    }
}
