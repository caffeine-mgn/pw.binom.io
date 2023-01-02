package pw.binom.collections

import kotlin.test.Test

class HashMap2Test {
    @Test
    fun removeTest() {
//        val bb = ArrayList<Int>()
//        bb.add(1)
//        bb.add(2)
//        bb.add(3)
//        bb.iterator().remove()
//        return

        val map = HashMap2<Int, Int>()
        map.put(1, 1)
        map.put(2, 2)

        val it = map.iterator()
        while (it.hasNext()) {
            val e = it.next()
            println("Remove! $e")
            it.remove()
        }
    }
}
