package pw.binom.wasm.node

import pw.binom.copyTo
import pw.binom.io.ByteArrayInput
import pw.binom.io.ByteArrayOutput
import pw.binom.io.Input
import pw.binom.io.use
import pw.binom.wasm.MemoryId
import pw.binom.wasm.visitors.DataSectionVisitor
import pw.binom.wasm.visitors.ExpressionsVisitor
import kotlin.js.JsName

class DataSection : DataSectionVisitor, MutableList<Data> by ArrayList() {
  private var memory: MemoryId = MemoryId(0u)
  private var exp: Expressions? = null
  private var active=false

  override fun active(memoryId: MemoryId): ExpressionsVisitor {
    memory = memoryId
    val e = Expressions()
    this.exp = e
    active=true
    return e
  }

  override fun active(): ExpressionsVisitor {
    memory = MemoryId(0u)
    val e = Expressions()
    this.exp = e
    active=true
    return e
  }

  override fun passive() {
    memory = MemoryId(0u)
    exp = null
    active=false
  }

  override fun data(input: Input) {
    val data = ByteArrayOutput().use {
      input.copyTo(it)
      it.toByteArray()
    }
    this += Data(
      memoryId = memory,
      expressions = exp,
      data = data,
      active = active
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
      require(!data.active || data.memoryId.raw != 0u)
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
