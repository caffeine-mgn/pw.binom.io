package pw.binom.wasm.node.inst

import pw.binom.wasm.LabelId
import pw.binom.wasm.node.HeapType
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.ValueVisitor

class BrOnCastFail(
  var flags: UByte,
  var labelId: LabelId,
) : Inst, ExpressionsVisitor.BrOnCastFailVisitor {
  var sourceImm = HeapType()
  var targetImm = HeapType()

  override fun target(): ValueVisitor.HeapVisitor {
    val e = HeapType()
    targetImm = e
    return e
  }

  override fun source(): ValueVisitor.HeapVisitor {
    val e = HeapType()
    sourceImm = e
    return e
  }

  override fun end() {
  }

  override fun start() {
  }

  override fun accept(visitor: ExpressionsVisitor) {
    val v = visitor.brOnCastFail(
      flags = flags,
      label = labelId,
    )
    v.start()
    sourceImm.accept(v.source())
    targetImm.accept(v.target())
    v.end()
  }
}
