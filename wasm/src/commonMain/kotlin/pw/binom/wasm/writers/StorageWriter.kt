package pw.binom.wasm.writers

import pw.binom.wasm.Types
import pw.binom.wasm.WasmOutput
import pw.binom.wasm.visitors.StorageVisitor
import pw.binom.wasm.visitors.ValueVisitor

class StorageWriter(private val out: WasmOutput) : StorageVisitor, StorageVisitor.PackVisitor {
  override fun pack(): StorageVisitor.PackVisitor = this

  override fun f16() {
    out.i8u(Types.TYPE_PAK_F16)
  }

  override fun i16() {
    out.i8u(Types.TYPE_PAK_I16)
  }

  override fun i8() {
    out.i8u(Types.TYPE_PAK_I8)
  }

  override fun valType(): ValueVisitor = ValueWriter(out)
}
