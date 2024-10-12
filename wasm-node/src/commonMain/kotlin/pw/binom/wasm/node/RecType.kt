package pw.binom.wasm.node

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.StorageVisitor
import pw.binom.wasm.visitors.TypeSectionVisitor
import pw.binom.wasm.visitors.TypeSectionVisitor.RecTypeVisitor
import pw.binom.wasm.visitors.ValueVisitor
import kotlin.js.JsName

class RecType : RecTypeVisitor {
  class Recursive : TypeSectionVisitor.RecursiveVisitor {
    val types = ArrayList<SubType>()
    override fun start() {
      types.clear()
      super.start()
    }

    override fun type(): TypeSectionVisitor.SubTypeVisitor {
      val e = SubType()
      types += e
      return e
    }

    override fun end() {
      super.end()
    }

    fun accept(visitor: TypeSectionVisitor.RecursiveVisitor) {
      visitor.start()
      types.forEach {
        it.accept(visitor.type())
      }
      visitor.end()
    }
  }

  sealed class Composite {
    abstract fun accept(visitor: TypeSectionVisitor.CompositeTypeVisitor)
  }

  class ArrayType : TypeSectionVisitor.ArrayVisitor, Composite() {

    var mutable = false
    var shared = false

    @JsName("typeF")
    var type: StorageType? = null

    override fun start(shared: Boolean) {
      this.shared = shared
    }

    override fun end() {
      super.end()
    }

    override fun type(): StorageVisitor {
      val e = StorageType()
      type = e
      return e
    }

    override fun mutable(value: Boolean) {
      this.mutable = mutable
    }

    fun accept(visitor: TypeSectionVisitor.ArrayVisitor) {
      visitor.start(shared)
      type!!.accept(visitor.type())
      visitor.mutable(mutable)
      visitor.end()
    }

    override fun accept(visitor: TypeSectionVisitor.CompositeTypeVisitor) {
      accept(visitor.array())
    }
  }

  class Field(val type: StorageType, val mutable: Boolean)

  class StructType : TypeSectionVisitor.StructTypeVisitor, Composite() {
    var shared = false
    val fields = ArrayList<Field>()
    private var fieldType: StorageType? = null
    override fun start(shared: Boolean) {
      this.shared = shared
      fields.clear()
    }

    override fun end() {
      super.end()
    }

    override fun fieldStart(): StorageVisitor {
      val e = StorageType()
      fieldType = e
      return e
    }

    override fun fieldEnd(mutable: Boolean) {
      fields += Field(
        type = fieldType!!,
        mutable = mutable,
      )
    }

    fun accept(visitor: TypeSectionVisitor.StructTypeVisitor) {
      visitor.start(shared)
      fields.forEach {
        it.type.accept(visitor.fieldStart())
        visitor.fieldEnd(mutable = it.mutable)
      }
      visitor.end()
    }

    override fun accept(visitor: TypeSectionVisitor.CompositeTypeVisitor) {
      accept(visitor.struct())
    }
  }

  class FuncType : TypeSectionVisitor.FuncTypeVisitor, Composite() {
    var args = ArrayList<ValueType>()
    var results = ArrayList<ValueType>()
    var shared = false
    override fun start(shared: Boolean) {
      this.shared = shared
      args.clear()
      results.clear()
    }

    override fun arg(): ValueVisitor {
      val e = ValueType()
      args += e
      return e
    }

    override fun result(): ValueVisitor {
      val e = ValueType()
      results += e
      return e
    }

    fun accept(visitor: TypeSectionVisitor.FuncTypeVisitor) {
      visitor.start(shared)
      args.forEach {
        it.accept(visitor.arg())
      }
      results.forEach {
        it.accept(visitor.result())
      }
      visitor.end()
    }

    override fun accept(visitor: TypeSectionVisitor.CompositeTypeVisitor) {
      accept(visitor.function())
    }
  }

  class CompositeType : TypeSectionVisitor.CompositeTypeVisitor {
    var type: Composite? = null

    override fun array(): TypeSectionVisitor.ArrayVisitor {
      val e = ArrayType()
      type = e
      return e
    }

    override fun struct(): TypeSectionVisitor.StructTypeVisitor {
      val e = StructType()
      type = e
      return e
    }

    override fun function(): TypeSectionVisitor.FuncTypeVisitor {
      val e = FuncType()
      type = e
      return e
    }

    fun accept(visitor: TypeSectionVisitor.CompositeTypeVisitor) {
      type!!.accept(visitor)
    }
  }

  class SubTypeWithParentType : TypeSectionVisitor.SubTypeWithParentVisitor {
    val parents = ArrayList<TypeId>()

    @JsName("typeF")
    var type: CompositeType? = null

    override fun start() {
      parents.clear()
      super.start()
    }

    override fun end() {
      super.end()
    }

    override fun parent(type: TypeId) {
      parents += type
    }

    override fun type(): TypeSectionVisitor.CompositeTypeVisitor {
      val e = CompositeType()
      type = e
      return e
    }

    fun accept(visitor: TypeSectionVisitor.SubTypeWithParentVisitor) {
      visitor.start()
      parents.forEach {
        visitor.parent(it)
      }
      type!!.accept(visitor.type())
      visitor.end()
    }
  }

  class SubType : TypeSectionVisitor.SubTypeVisitor {
    var type: SubTypeWithParentType? = null
    var final: SubTypeWithParentType? = null
    var nonFinal: SubTypeWithParentType? = null

    @JsName("singleF")
    var single: CompositeType? = null
    override fun withParent(): TypeSectionVisitor.SubTypeWithParentVisitor {
      val e = SubTypeWithParentType()
      nonFinal = e
      return e
    }

    override fun withParentFinal(): TypeSectionVisitor.SubTypeWithParentVisitor {
      val e = SubTypeWithParentType()
      final = e
      return e
    }

    override fun single(): TypeSectionVisitor.CompositeTypeVisitor {
      val e = CompositeType()
      single = e
      return e
    }

    fun accept(visitor: TypeSectionVisitor.SubTypeVisitor) {
      when {
        single != null -> single!!.accept(visitor.single())
        final != null -> final!!.accept(visitor.withParentFinal())
        nonFinal != null -> nonFinal!!.accept(visitor.withParent())
        else -> throw IllegalStateException()
      }
    }
  }

  @JsName("recursiveF")
  var recursive: Recursive? = null

  @JsName("singleF")
  var single: SubType? = null

  override fun recursive(): TypeSectionVisitor.RecursiveVisitor {
    val e = Recursive()
    recursive = e
    return e
  }

  override fun single(): TypeSectionVisitor.SubTypeVisitor {
    val e = SubType()
    single = e
    return e
  }

  fun accept(visitor: RecTypeVisitor) {
    when {
      single != null -> single!!.accept(visitor.single())
      recursive != null -> recursive!!.accept(visitor.recursive())
      else -> throw IllegalStateException()
    }
  }
}
