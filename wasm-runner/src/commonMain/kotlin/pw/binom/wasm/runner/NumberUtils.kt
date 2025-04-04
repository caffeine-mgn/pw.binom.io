package pw.binom.wasm.runner

object NumberUtils {
  fun getU(value: Value): Value = when (value) {
    is Value.Primitive.F32 -> TODO()
    is Value.Primitive.F64 -> TODO()
    is Value.Primitive.I16 -> {
      Value.Primitive.I32(value.value.toUInt().toInt())
    }

    is Value.Primitive.I32 -> value
    is Value.Primitive.I64 -> value
    is Value.Primitive.I8 -> {
      Value.Primitive.I32(value.value.toUInt().toInt())
    }

    Value.Ref.INVALID -> TODO()
    else -> TODO()
  }

  fun getS(value: Value): Value = when (value) {
    is Value.Primitive.F32 -> TODO()
    is Value.Primitive.F64 -> TODO()
    is Value.Primitive.I16 -> {
      Value.Primitive.I32(value.value.toInt())
    }

    is Value.Primitive.I32 -> value
    is Value.Primitive.I64 -> value
    is Value.Primitive.I8 -> {
      Value.Primitive.I32(value.value.toInt())
    }

    Value.Ref.INVALID -> TODO()
    else -> TODO()
  }
}
