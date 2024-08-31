package pw.binom.wasm.dom

import pw.binom.wasm.PackPrimitive
import pw.binom.wasm.visitors.StorageVisitor
import pw.binom.wasm.visitors.ValueVisitor

class StorageType : StorageVisitor {
  var valueVisitor: ValueType? = null
  var packPrimitive: PackPrimitive? = null

  private val packVisitor = object : StorageVisitor.PackVisitor {
    override fun i8() {
      packPrimitive = PackPrimitive.I8
    }

    override fun i16() {
      packPrimitive = PackPrimitive.I16
    }

    override fun f16() {
      packPrimitive = PackPrimitive.F16
    }
  }

  override fun pack(): StorageVisitor.PackVisitor = packVisitor

  override fun valType(): ValueVisitor {
    val e = ValueType()
    valueVisitor = e
    return e
  }

  fun accept(visitor: StorageVisitor) {
    when {
      valueVisitor != null -> valueVisitor!!.accept(visitor.valType())
      packPrimitive != null -> {
        val v = visitor.pack()
        when (packPrimitive!!) {
          PackPrimitive.I8 -> v.i8()
          PackPrimitive.I16 -> v.i16()
          PackPrimitive.F16 -> v.f16()
        }
      }
    }
  }
}
