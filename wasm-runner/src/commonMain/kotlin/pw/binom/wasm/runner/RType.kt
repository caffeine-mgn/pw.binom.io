package pw.binom.wasm.runner

import pw.binom.wasm.TypeId

sealed interface RType {
  sealed interface Primitive : RType {
    val sizeInBytes: Int

    data object I8 : Primitive {
      override val sizeInBytes: Int
        get() = 1
    }

    data object I16 : Primitive {
      override val sizeInBytes: Int
        get() = 2
    }

    data object I32 : Primitive {
      override val sizeInBytes: Int
        get() = 4
    }

    data object I64 : Primitive {
      override val sizeInBytes: Int
        get() = 8
    }

    data object F16 : Primitive {
      override val sizeInBytes: Int
        get() = 2
    }

    data object F32 : Primitive {
      override val sizeInBytes: Int
        get() = 4
    }

    data object F64 : Primitive {
      override val sizeInBytes: Int
        get() = 8
    }
  }

  data class Function(val args: List<VType>, val results: List<VType>, val shared: Boolean) : RType

  sealed interface Ref : RType {
    data class Object(
      val fields: List<Field>,
      val parents: List<Object>,
      val final: Boolean,
      val id: TypeId,
    ) : Ref

    data class Field(val mutable: Boolean, val type: VType)
    data class Array(val mutable: Boolean, val type: VType) : Ref
  }
}
