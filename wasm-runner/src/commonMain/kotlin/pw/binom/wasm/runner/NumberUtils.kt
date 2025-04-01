package pw.binom.wasm.runner

object NumberUtils {
  fun getU(value: Value): Value = when (value) {
    is Value.Primitive.F32 -> TODO()
    is Value.Primitive.F64 -> TODO()
    is Value.Primitive.I16 -> {
      val e = Value.Primitive.I32(value.value.toUInt().toInt())
      ValueHistory.self.add(e, "Преобразование", mapOf("from" to value))
      e
    }

    is Value.Primitive.I32 -> value
    is Value.Primitive.I64 -> value
    is Value.Primitive.I8 -> {
      val e = Value.Primitive.I32(value.value.toUInt().toInt())
      ValueHistory.self.add(e, "Преобразование", mapOf("from" to value))
      e
    }

    Value.Ref.INVALID -> TODO()
    else -> TODO()
  }

  fun getS(value: Value): Value = when (value) {
    is Value.Primitive.F32 -> TODO()
    is Value.Primitive.F64 -> TODO()
    is Value.Primitive.I16 -> {
      val e = Value.Primitive.I32(value.value.toInt())
      ValueHistory.self.add(e, "Преобразование", mapOf("from" to value))
      e
    }

    is Value.Primitive.I32 -> value
    is Value.Primitive.I64 -> value
    is Value.Primitive.I8 -> {
      val e = Value.Primitive.I32(value.value.toInt())
      ValueHistory.self.add(e, "Преобразование", mapOf("from" to value))
      e
    }

    Value.Ref.INVALID -> TODO()
    else -> TODO()
  }
}
