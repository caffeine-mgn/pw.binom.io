package pw.binom.wasm.runner

import pw.binom.wasm.FunctionId

sealed interface Value {

  sealed interface Ref : Value {
    data class Array(val type: RType.Ref.Array, val values: MutableList<Value>) : Ref {
      val elementType: VType
        get() = type.type
      val mutable: Boolean
        get() = type.mutable
    }

    data class RefFunction(val id: FunctionId) : Ref
    data object NULL : Ref
    data object INVALID : Ref
  }

  sealed interface Primitive : Value {
    data class I8(val value: Byte) : Primitive
    data class I16(val value: Short) : Primitive
    data class I32(val value: Int) : Primitive
    data class I64(val value: Long) : Primitive
    data class F32(val value: Float) : Primitive
    data class F64(val value: Double) : Primitive
  }

  val asI32
    get() = this as Primitive.I32
}
