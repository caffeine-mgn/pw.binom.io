package pw.binom.strong

import pw.binom.strong.exceptions.CycleDependencyException

internal object GraphUtils {
    fun <T : Any> buildDependencyGraph(
        elements: Collection<T>,
        subNodeProvider: (T) -> Collection<T>,
    ): List<T> {
        val order = ArrayList<T>()
        val inited = HashSet<T>()
        val initing = HashSet<T>()
        val treePath = ArrayList<T>()
        fun init(e: T) {
            println("init($e)")
            if (e in inited) {
                return
            }
            if (e in initing) {
                TODO()
            }
            initing += e
            treePath += e
            val subNodes = subNodeProvider(e)
            subNodes.forEach {

                if (it in initing) {
                    throw CycleException(treePath+listOf(it))
                }
                init(it)
            }
            inited += e
            initing -= e
            order += e
        }
        elements.forEach {
            init(it)
            treePath.clear()
        }
        return order
    }

    class CycleException(val dependenciesPath: List<Any>) : RuntimeException(){
        override val message: String?
            get() {
                val sb = StringBuilder()
                dependenciesPath.forEachIndexed { index, beanDescription ->
                    if (index > 0) {
                        sb.append(" -> ")
                    }
                    sb.append(beanDescription)
                }
                return sb.toString()
            }
    }
}