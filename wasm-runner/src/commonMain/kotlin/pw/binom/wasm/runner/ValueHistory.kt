package pw.binom.wasm.runner

import pw.binom.collection.WeakReferenceMap
import pw.binom.collection.getOrPut
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
@Deprecated(level = DeprecationLevel.ERROR, message = "Internal use only")
@PublishedApi
internal var current: ValueHistory? = null

class ValueHistory {
  companion object {
    @Suppress("DEPRECATION_ERROR")
    val self
      get() = current!!
  }

  @Suppress("DEPRECATION_ERROR")
  inline fun <T> use(func: () -> T): T {
    val old = current
    current = this
    return try {
      func()
    } finally {
      current = old
    }
  }

  class Info(val title: String, val meta: Map<String, Value>)

  private val e = WeakReferenceMap<Value, ArrayList<Info>>()
  fun add(value: Value, title: String, meta: Map<String, Value> = emptyMap()) {
    e.getOrPut(value) { ArrayList() }.add(Info(title = title, meta = meta))
  }

  fun add(value: Value, from: Value) {

  }

  fun get(value: Value) = e[value]
  fun print(title: String, value: Value, level: Int = 0) {
    fun printPadding() {
      repeat(level) {
        print("  ")
      }
    }
    printPadding()
    println("===$title===")
    printPadding()
    print(value = value, level = level)
  }

  fun print(value: Value, level: Int = 0) {
    fun printPadding() {
      repeat(level) {
        print("  ")
      }
    }
    printPadding()
    println("Значение: $value")
    val info = e[value]
    if (info == null) {
      printPadding()
      println("нет информации")
    } else {
      info.forEach {
        printInfo(it, level)
      }
    }
  }

  private fun printInfo(info: Info, padding: Int) {
    fun printPadding() {
      repeat(padding) {
        print("  ")
      }
    }
    printPadding()
    println("---${info.title}---")
//    if (info.meta.isNotEmpty()) {
//      printPadding()
//      info.meta.forEach { (key, value) ->
//        println("--->$key")
//        print(value = value, level = padding + 1)
//      }
//    }
  }
}
