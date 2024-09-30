package pw.binom.wasm.node

import pw.binom.copyTo
import pw.binom.io.ByteArrayInput
import pw.binom.io.ByteArrayOutput
import pw.binom.io.Input
import pw.binom.io.use
import pw.binom.wasm.MemoryId
import pw.binom.wasm.visitors.DataSectionVisitor
import pw.binom.wasm.visitors.ExpressionsVisitor

class DataSection : DataSectionVisitor, MutableList<Data> by ArrayList() {
  private var memory: MemoryId = MemoryId(0u)
  private var exp: Expressions? = null

  override fun active(memoryId: MemoryId): ExpressionsVisitor {
    memory = memoryId
    val e = Expressions()
    this.exp = e
    return e
  }

  override fun active(): ExpressionsVisitor {
    memory = MemoryId(0u)
    val e = Expressions()
    this.exp = e
    return e
  }

  override fun passive() {
    memory = MemoryId(0u)
    exp = null
  }

  override fun data(input: Input) {
    val data = ByteArrayOutput().use {
      input.copyTo(it)
      it.toByteArray()
    }
    this += Data(
      memoryId = memory,
      expressions = exp,
      data = data
    )
  }

  override fun elementStart() {
    super.elementStart()
  }

  override fun elementEnd() {
    exp = null
  }

  override fun start() {
    clear()
    super.start()
  }

  override fun end() {
    super.end()
  }

  fun accept(visitor: DataSectionVisitor) {
    visitor.start()
    forEach { data ->
      visitor.elementStart()
      if (data.memoryId == null) {
        if (data.expressions == null) {
          visitor.passive()
        } else {
          data.expressions!!.accept(visitor.active())
        }
      } else {
        check(data.expressions != null)
        visitor.active(data.memoryId!!)
        data.expressions!!.accept(visitor.active())
      }
      ByteArrayInput(data.data).use { d ->
        visitor.data(d)
      }
      visitor.elementEnd()
    }
    visitor.end()
  }
}
