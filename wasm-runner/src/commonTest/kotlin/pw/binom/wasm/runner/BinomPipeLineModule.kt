package pw.binom.wasm.runner

class BinomPipeLineModule(val data: ByteArray) : ImportResolver {
  override fun func(module: String, field: String, type: RType.Function): ((ExecuteContext) -> Unit)? {
    if (module != "binom") {
      return null
    }

    return when (field) {
      "get_input_size" -> { e ->
        e.pushResult(Value.Primitive.I32(data.size))
      }

      "get_input_data" -> { e ->
        val address = e.args[0] as Value.Primitive.I32
        e.runner.memory[0].pushBytes(
          src = data,
          offset = address.value.toUInt(),
        )
        e.pushResult(Value.Primitive.I32(1))
      }
      else -> super.func(module, field, type)
    }
  }
}
