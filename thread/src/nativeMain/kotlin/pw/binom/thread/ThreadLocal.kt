package pw.binom.thread

import pw.binom.collections.defaultMutableMap
import pw.binom.collections.useName
import kotlin.concurrent.AtomicInt

actual class ThreadLocal<T> actual constructor() {
  private val lock = AtomicInt(0)
  private val map = defaultMutableMap<Thread, T>().useName("ThreadLocal.map")
  private var counter = 0

  private inline fun <T> locking(func: () -> T): T {
    while (true) {
      if (lock.compareAndSet(0, 1)) {
        break
      }
    }
    counter++
    if (counter > 500) {
      val it = map.iterator()
      while (it.hasNext()) {
        val e = it.next()
        if (!e.key.isActive) {
          it.remove()
        }
      }
    }
    try {
      return func()
    } finally {
      lock.value = 0
    }
  }

  actual fun get(): T? = locking {
    map[Thread.currentThread]
  }

  actual fun set(value: T) {
    locking {
      map[Thread.currentThread] = value
    }
  }

  actual fun remove() {
    locking {
      map.remove(Thread.currentThread)
    }
  }
}
