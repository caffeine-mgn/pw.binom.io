package pw.binom.strong

import kotlin.test.Test
import kotlin.test.fail

class GraphUtilsTest {

    class Node(val id: Int) {
        val deps = ArrayList<Node>()
        override fun toString(): String = "$id"
    }

    @Test
    fun test() {
        val n0 = Node(0)
        val n1 = Node(1)
        val n2 = Node(2)
        n0.deps += n1
        n1.deps += n2
        n2.deps += n0

        try {
            GraphUtils.buildDependencyGraph(listOf(n0, n1, n2)) { it.deps }
            fail()
        } catch (e: GraphUtils.CycleException) {
            // Do nothing
        }
    }
}
