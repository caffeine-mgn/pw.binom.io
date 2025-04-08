package pw.binom.wasm.runner

class Instance(val type: RType.Ref.Object, val fields: List<Field>):Value.Ref {
  companion object {
    fun create(type: RType.Ref.Object, fields: List<Value>): Instance {
      require(fields.size == type.fields.size)
      val valuesIt = fields.iterator()
      val initedFields = type.fields.mapIndexed { index, it ->
        if (it.mutable) {
          MutableFieldImpl(it.type, valuesIt.next())
        } else {
          FieldImpl(it.type, valuesIt.next())
        }
      }
      return Instance(type = type, fields = initedFields)
    }
  }

  interface Field {
    val type: VType
    val value: Value
  }

  interface MutableField : Field {
    override var value: Value
  }

  private class FieldImpl(override val type: VType, override val value: Value) : Field{
    override fun toString(): String = "FieldImpl(value=$value)"
  }
  private data class MutableFieldImpl(override val type: VType, override var value: Value) : MutableField{
    override fun toString(): String = "MutableFieldImpl(value=$value)"
  }

  override fun toString(): String = "Instance(fields=$fields)"
}
