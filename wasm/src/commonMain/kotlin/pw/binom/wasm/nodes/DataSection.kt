package pw.binom.wasm.nodes

import pw.binom.copyTo
import pw.binom.io.ByteArrayInput
import pw.binom.io.ByteArrayOutput
import pw.binom.io.Input
import pw.binom.io.use
import pw.binom.wasm.MemoryId
import pw.binom.wasm.visitors.DataSectionVisitor
import pw.binom.wasm.visitors.ExpressionsVisitor

class DataSection : DataSectionVisitor {
  val elements = ArrayList<Data>()
  private var memory: MemoryId? = null
  private var e: Expressions? = null

  override fun active(memoryId: MemoryId): ExpressionsVisitor {
    memory = memoryId
    val e = Expressions()
    this.e = e
    return e
  }

  override fun active(): ExpressionsVisitor {
    memory = null
    val e = Expressions()
    this.e = e
    return e
  }

  override fun passive() {
    memory = null
    e = null
  }

  override fun data(input: Input) {
    val data = ByteArrayOutput().use {
      input.copyTo(it)
      it.toByteArray()
    }
    elements += Data(
      memoryId = memory,
      expressions = e,
      data = data
    )
  }

  override fun elementStart() {
    super.elementStart()
  }

  override fun elementEnd() {
    memory = null
    e = null
  }

  override fun start() {
    elements.clear()
    super.start()
  }

  override fun end() {
    super.end()
  }

  fun accept(visitor: DataSectionVisitor) {
    visitor.start()
    elements.forEach {
      visitor.elementStart()
      if (it.memoryId == null) {
        if (it.expressions == null) {
          visitor.passive()
        } else {
          it.expressions!!.accept(visitor.active())
        }
      } else {
        check(it.expressions != null)
        visitor.active(it.memoryId!!)
        it.expressions!!.accept(visitor.active())
      }
      ByteArrayInput(it.data).use { d ->
        visitor.data(d)
      }
      visitor.elementEnd()
    }
    visitor.end()
  }
}
