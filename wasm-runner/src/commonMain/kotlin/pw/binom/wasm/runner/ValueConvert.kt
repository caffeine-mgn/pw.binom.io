package pw.binom.wasm.runner

object ValueConvert {
  fun convert(value: Value, type: VType, typeDictionary: TypeDictionary): Value {
    if (type is VType.Ref) {
      val targetType = typeDictionary.getRType(type.id)
      if (value is Instance && value.type == targetType) {
        return value
      }
    }
    TODO()
  }
}
