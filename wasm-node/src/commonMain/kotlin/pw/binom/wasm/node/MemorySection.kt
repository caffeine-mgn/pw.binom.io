package pw.binom.wasm.node

import pw.binom.wasm.visitors.MemorySectionVisitor

class MemorySection : MemorySectionVisitor, MutableList<MemoryLimit> by ArrayList() {
  override fun end() {
    super.end()
  }

  override fun memory(inital: UInt, max: UInt) {
    this += MemoryLimit(inital = inital, max = max)
  }

  override fun memory(inital: UInt) {
    this += MemoryLimit(inital = inital, max = null)
  }

  override fun start() {
    clear()
    super.start()
  }

  fun accept(visitor: MemorySectionVisitor) {
    visitor.start()
    forEach {
      if (it.max == null) {
        visitor.memory(inital = it.inital)
      } else {
        visitor.memory(inital = it.inital, max = it.max)
      }
    }
    visitor.end()
  }
}
