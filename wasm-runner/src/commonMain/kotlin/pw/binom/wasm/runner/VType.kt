package pw.binom.wasm.runner

import pw.binom.wasm.AbsoluteHeapType
import pw.binom.wasm.TypeId

sealed interface VType {
  val default: Value

  data class Primitive(val type: RType.Primitive) : VType {
    override val default: Value
      get() = when (type) {
        RType.Primitive.F16 -> TODO()
        RType.Primitive.F32 -> Value.Primitive.F32(0f)
        RType.Primitive.F64 -> Value.Primitive.F64(0.0)
        RType.Primitive.I16 -> Value.Primitive.I16(0)
        RType.Primitive.I32 -> Value.Primitive.I32(0)
        RType.Primitive.I64 -> Value.Primitive.I64(0)
        RType.Primitive.I8 -> Value.Primitive.I8(0)
      }
  }

  data class Ref(val id: TypeId, val nullable: Boolean) : VType {
    override val default: Value
      get() = if (nullable) Value.Ref.NULL else Value.Ref.INVALID
  }

  data class RefAbsolute(val type: AbsoluteHeapType, val nullable: Boolean) : VType {
    override val default: Value
      get() = Value.Ref.NULL
  }
}
