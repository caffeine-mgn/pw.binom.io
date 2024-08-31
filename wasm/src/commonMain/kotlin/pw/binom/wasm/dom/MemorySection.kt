package pw.binom.wasm.dom

import pw.binom.wasm.visitors.MemorySectionVisitor

class MemorySection : MemorySectionVisitor {
  val elements = ArrayList<Memory>()
  override fun end() {
    super.end()
  }

  override fun memory(inital: UInt, max: UInt) {
    elements += Memory(inital = inital, max = max)
  }

  override fun memory(inital: UInt) {
    elements += Memory(inital = inital, max = null)
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
