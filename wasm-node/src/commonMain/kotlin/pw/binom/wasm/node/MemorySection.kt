package pw.binom.wasm.node

import pw.binom.wasm.visitors.MemorySectionVisitor

class MemorySection : MemorySectionVisitor {
  val elements = ArrayList<MemoryLimit>()
  override fun end() {
    super.end()
  }

  override fun memory(inital: UInt, max: UInt) {
    elements += MemoryLimit(inital = inital, max = max)
  }

  override fun memory(inital: UInt) {
    elements += MemoryLimit(inital = inital, max = null)
  }

  override fun start() {
    elements.clear()
    super.start()
  }

  fun accept(visitor: MemorySectionVisitor) {
    visitor.start()
    elements.forEach {
      if (it.max == null) {
        visitor.memory(inital = it.inital)
      } else {
        visitor.memory(inital = it.inital, max = it.max)
      }
    }
    visitor.end()
  }
}
