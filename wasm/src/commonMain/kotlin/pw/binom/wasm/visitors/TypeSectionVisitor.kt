package pw.binom.wasm.visitors

import pw.binom.wasm.TypeId

interface TypeSectionVisitor {
  companion object {
    val SKIP = object : TypeSectionVisitor {}
  }

  fun start() {}
  fun recType(): RecTypeVisitor = RecTypeVisitor.SKIP
  fun end() {}

  interface FuncTypeVisitor {
    companion object {
      val SKIP = object : FuncTypeVisitor {}
    }

    fun start(shared: Boolean) {}
    fun end() {}
    fun arg(): ValueVisitor = ValueVisitor.SKIP
    fun result(): ValueVisitor = ValueVisitor.SKIP
  }

  interface StructTypeVisitor {
    companion object {
      val SKIP = object : StructTypeVisitor {}
    }

    fun start(shared: Boolean) {}
    fun end() {}
    fun fieldStart(): StorageVisitor = StorageVisitor.SKIP
    fun fieldEnd(mutable: Boolean) {}
  }

  interface ArrayVisitor {
    companion object {
      val SKIP = object : ArrayVisitor {}
    }

    fun start(shared: Boolean) {}
    fun end() {}
    fun type(): StorageVisitor = StorageVisitor.SKIP
    fun mutable(value: Boolean) {}
  }

  interface CompositeTypeVisitor {
    companion object {
      val SKIP = object : CompositeTypeVisitor {}
    }

    fun array(): ArrayVisitor = ArrayVisitor.SKIP
    fun struct(): StructTypeVisitor = StructTypeVisitor.SKIP
    fun function(): FuncTypeVisitor = FuncTypeVisitor.SKIP
  }

  interface SubTypeWithParentVisitor {
    companion object {
      val SKIP = object : SubTypeWithParentVisitor {}
    }

    fun start() {}
    fun end() {}
    fun parent(type: TypeId) {}
    fun type(): CompositeTypeVisitor = CompositeTypeVisitor.SKIP
  }

  interface SubTypeVisitor {
    companion object {
      val SKIP = object : SubTypeVisitor {}
    }

    fun withParent(): SubTypeWithParentVisitor = SubTypeWithParentVisitor.SKIP
    fun withParentFinal(): SubTypeWithParentVisitor = SubTypeWithParentVisitor.SKIP
    fun single(): CompositeTypeVisitor = CompositeTypeVisitor.SKIP
  }

  interface RecursiveVisitor {
    companion object {
      val SKIP = object : RecursiveVisitor {}
    }

    fun start() {}
    fun type(): SubTypeVisitor = SubTypeVisitor.SKIP
    fun end() {}
  }

  interface RecTypeVisitor {
    companion object {
      val SKIP = object : RecTypeVisitor {}
    }

    fun recursive(): RecursiveVisitor = RecursiveVisitor.SKIP
    fun single(): SubTypeVisitor = SubTypeVisitor.SKIP
  }
}
