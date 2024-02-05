package pw.binom.strong

import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableSet

internal object GraphUtils {
  fun <T : Any> buildDependencyGraph(
    elements: Collection<T>,
    subNodeProvider: (T) -> Collection<T>,
  ): List<T> {
//        val order = ArrayList<T>()
    val inited = defaultMutableSet<T>()
//        val initing = HashSet<T>()
//        val treePath = ArrayList<T>()

    val order = defaultMutableList<T>()
//        val inited = defaultHashSet<T>()
    val initing = defaultMutableSet<T>()
    val treePath = defaultMutableList<T>()

    fun init(
      e: T,
      path: LinkedHashSet<T>,
    ) {
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
        if (it in path) {
          throw CycleException(path.toList())
        }
        val newPath = LinkedHashSet(path)
        newPath += it
        init(it, newPath)
      }
      inited += e
      if (!initing.remove(e)) {
        TODO("$e not in $initing")
      }
      if (e in initing) {
        TODO("$e steel in initing")
      }
      order += e
    }
    elements.forEach {
      init(it, linkedSetOf(it))
      treePath.clear()
    }
    return order
  }

  class CycleException(val dependenciesPath: List<Any>) : RuntimeException() {
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
